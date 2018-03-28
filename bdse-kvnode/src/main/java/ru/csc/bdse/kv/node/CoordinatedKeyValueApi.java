package ru.csc.bdse.kv.node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.csc.bdse.kv.db.RecordWithTimestamp;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CoordinatedKeyValueApi implements KeyValueApi {

    private final int rcl;
    private final int wcl;
    private final int timeout;
    private final List<InternalKeyValueApi> apis;
    private final ObjectMapper mapper;
    private final ConflictResolver conflictResolver;

    public CoordinatedKeyValueApi(
            int rcl, int wcl, int timeout, List<InternalKeyValueApi> apis, ObjectMapper mapper, ConflictResolver cr) {
        this.rcl = rcl;
        this.wcl = wcl;
        this.timeout = timeout;
        this.apis = apis;
        this.mapper = mapper;
        this.conflictResolver = cr;
    }

    @Override
    public void put(String key, byte[] value) {
        ExecutorService executorService = Executors.newFixedThreadPool(apis.size());
        ExecutorService monitorService = Executors.newFixedThreadPool(apis.size());

        AtomicInteger successCount = new AtomicInteger(0);

        apis.forEach(api -> {
            Future<Void> task = executorService.submit(() -> {
                api.put(key, value);
                return null;
            });

            monitorService.submit(() -> {
                try {
                    task.get(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e.getMessage());
                } catch (TimeoutException e) {
                    // this is fine
                }
                successCount.getAndIncrement();
                return null;
            });
        });

        try {
            monitorService.awaitTermination(2 * timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

        int finalCount = successCount.get();
        if(finalCount < wcl) {
            throw new RuntimeException("'put' operation failed: not enough replica writes succeeded");
        }
    }

    @Override
    public Optional<byte[]> get(String key) {
        ExecutorService executorService = Executors.newFixedThreadPool(apis.size());
        ExecutorService monitorService = Executors.newFixedThreadPool(apis.size());

        CopyOnWriteArrayList<Optional<byte[]>> results = new CopyOnWriteArrayList<>();

        apis.forEach(api -> {
            Future<Optional<byte[]>> task = executorService.submit(() -> api.get(key));

            monitorService.submit(() -> {
                try {
                    Optional<byte[]> result = task.get(timeout, TimeUnit.SECONDS);
                    results.add(result);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e.getMessage());
                } catch (TimeoutException e) {
                    // this is fine
                }
                return null;
            });
        });

        try {
            monitorService.awaitTermination(2 * timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

        if(results.size() < rcl) {
            throw new RuntimeException("'get' operation failed: not enough replica reads secceeded");
        }

        return conflictResolver.resolve(results);
    }

    @Override
    public Set<String> getKeys(String prefix) {
        ExecutorService executorService = Executors.newFixedThreadPool(apis.size());
        ExecutorService monitorService = Executors.newFixedThreadPool(apis.size());

        AtomicInteger successCount = new AtomicInteger(0);
        ConcurrentSkipListSet<String> results = new ConcurrentSkipListSet<>();

        apis.forEach(api -> {
            Future<Void> task = executorService.submit(() -> {
                Set<String> result = api.getKeys(prefix);
                results.addAll(result);
                return null;
            });

            monitorService.submit(() -> {
                try {
                    task.get(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e.getMessage());
                } catch (TimeoutException e) {
                    // this is fine
                }
                successCount.getAndIncrement();
                return null;
            });
        });

        try {
            monitorService.awaitTermination(2 * timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

        int finalCount = successCount.get();
        if(finalCount < rcl) {
            throw new RuntimeException("'put' operation failed: not enough replica writes succeeded");
        }

        return results;
    }

    @Override
    public void delete(String key) {
        byte[] value;
        try {
            value = mapper.writeValueAsBytes(new RecordWithTimestamp(true));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        put(key, value);
    }

    @Override
    public Set<NodeInfo> getInfo() {
        ExecutorService executorService = Executors.newFixedThreadPool(apis.size());
        ExecutorService monitorService = Executors.newFixedThreadPool(apis.size());

        AtomicInteger successCount = new AtomicInteger(0);
        ConcurrentSkipListSet<NodeInfo> results = new ConcurrentSkipListSet<>();

        apis.forEach(api -> {
            Future<Void> task = executorService.submit(() -> {
                results.addAll(api.getInfo());
                return null;
            });

            monitorService.submit(() -> {
                try {
                    task.get(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e.getMessage());
                } catch (TimeoutException e) {
                    // this is fine
                }
                successCount.getAndIncrement();
                return null;
            });
        });

        try {
            monitorService.awaitTermination(2 * timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

        int finalCount = successCount.get();
        if(finalCount < rcl) {
            throw new RuntimeException("'getInfo' operation failed: not enough replica writes succeeded");
        }

        return results;
    }

    @Override
    public void action(String node, NodeAction action) {
        ExecutorService executorService = Executors.newFixedThreadPool(apis.size());

        apis.forEach(api -> {
            Future<Void> task = executorService.submit(() -> {
                api.action(node, action);
                return null;
            });
        });

        try {
            executorService.awaitTermination(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        ExecutorService executorService = Executors.newFixedThreadPool(apis.size());

        apis.forEach(api -> {
            Future<Void> task = executorService.submit(() -> {
                api.deleteAll();
                return null;
            });
        });

        try {
            executorService.awaitTermination(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}
