package ru.csc.bdse.kv.node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CoordinatedKeyValueApi implements KeyValueApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatedKeyValueApi.class);
    private static final Object EXECUTION_SUCCEEDED = new Object();

    private final int rcl;
    private final int wcl;
    private final int timeout;
    private final List<InternalKeyValueApi> apis;
    private final ObjectMapper mapper;
    private final ConflictResolver conflictResolver;

    private final ScheduledExecutorService executorService;

    public CoordinatedKeyValueApi(
            int rcl, int wcl, int timeout, List<InternalKeyValueApi> apis, ObjectMapper mapper, ConflictResolver cr) {
        this.rcl = rcl;
        this.wcl = wcl;
        this.timeout = timeout;
        this.apis = apis;
        this.mapper = mapper;
        this.conflictResolver = cr;

        executorService = Executors.newScheduledThreadPool(apis.size());
    }

    private <T> List<T> executeTasks(List<Supplier<T>> tasks, int enoughToComplete) {
        CountDownLatch latch = new CountDownLatch(enoughToComplete);
        // Work tasks
        List<Future<T>> futures = tasks.stream()
                .map(task -> executorService.submit(() -> {
                    T result = task.get();
                    latch.countDown();
                    return result;
                }))
                .collect(Collectors.toList());
        // Shutdown task
        executorService.schedule(() -> {
            futures.forEach(future -> future.cancel(true));
            while (latch.getCount() > 0)
                latch.countDown();
        }, timeout, TimeUnit.MILLISECONDS);
        // Wait for completion:
        //   either `enoughToComplete` completes,
        //   or timeout will force completion
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while awaiting for request to complete...", e);
        }
        // Gather all successes
        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted on the future that already must be completed of failed");
                    } catch (ExecutionException e) {
                        LOGGER.warn("Error in processing request, assuming failure", e.getCause());
                        return null;
                    } catch (CancellationException ignored) {
                        // Cancelled is OK
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void doPut(String key, RecordWithTimestamp record) {
        byte[] value = encodeRecord(record);
        List<Supplier<Object>> tasks = apis.stream()
                .map(api -> (Supplier<Object>) () -> {
                    api.put(key, value);
                    return EXECUTION_SUCCEEDED;
                })
                .collect(Collectors.toList());

        int succeeded = executeTasks(tasks, wcl).size();
        if (succeeded < wcl) {
            throw new RuntimeException("'put' operation failed: not enough replica writes succeeded");
        }
    }

    @Override
    public void put(String key, byte[] value) {
        doPut(key, RecordWithTimestamp.ofPresent(value, System.currentTimeMillis()));
    }

    @Override
    public Optional<byte[]> get(String key) {
        List<Supplier<Optional<RecordWithTimestamp>>> tasks = apis.stream()
                .map(api -> (Supplier<Optional<RecordWithTimestamp>>) () -> api.get(key).map(this::decodeRecord))
                .collect(Collectors.toList());

        List<Optional<RecordWithTimestamp>> results = executeTasks(tasks, rcl);
        if(results.size() < rcl) {
            throw new RuntimeException("'get' operation failed: not enough replica reads succeeded");
        }
        List<RecordWithTimestamp> filteredResults = results.stream()
                .map(r -> r.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return conflictResolver.resolve(filteredResults).flatMap(RecordWithTimestamp::getOptionalData);
    }

    @Override
    public Set<String> getKeys(String prefix) {
        List<Supplier<Set<String>>> tasks = apis.stream()
                .map(api -> (Supplier<Set<String>>) () -> api.getKeys(prefix))
                .collect(Collectors.toList());

        List<Set<String>> results = executeTasks(tasks, rcl);
        if (results.size() < rcl) {
            throw new RuntimeException("'put' operation failed: not enough replica writes succeeded");
        }

        return conflictResolver.resolveKeys(results);
    }

    @Override
    public void delete(String key) {
        doPut(key, RecordWithTimestamp.ofDeleted(System.currentTimeMillis()));
    }

    @Override
    public Set<NodeInfo> getInfo() {
        List<Supplier<Set<NodeInfo>>> tasks = apis.stream()
                .map(api -> (Supplier<Set<NodeInfo>>) api::getInfo)
                .collect(Collectors.toList());
        List<Set<NodeInfo>> results = executeTasks(tasks, apis.size());
        return results.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public void action(String node, NodeAction action) {
        List<Supplier<Object>> tasks = apis.stream()
                .map(api -> (Supplier<Object>) () -> {
                    api.action(node, action);
                    return EXECUTION_SUCCEEDED;
                })
                .collect(Collectors.toList());

        int succeeded = executeTasks(tasks, apis.size()).size();
        // Here we assume that if node is asked for action not about itself then it fails...
        if (succeeded == 0) {
            throw new RuntimeException("'deleteAll' operation failed: not enough replica writes succeeded");
        }
    }

    @Override
    public void deleteAll() {
        List<Supplier<Object>> tasks = apis.stream()
                .map(api -> (Supplier<Object>) () -> {
                    api.deleteAll();
                    return EXECUTION_SUCCEEDED;
                })
                .collect(Collectors.toList());

        int succeeded = executeTasks(tasks, apis.size()).size();
        if (succeeded < apis.size()) {
            throw new RuntimeException("'deleteAll' operation failed: not enough replica writes succeeded");
        }
    }

    private byte[] encodeRecord(RecordWithTimestamp record) {
        try {
            return mapper.writeValueAsBytes(record);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private RecordWithTimestamp decodeRecord(byte[] data) {
        try {
            return mapper.readValue(data, RecordWithTimestamp.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
