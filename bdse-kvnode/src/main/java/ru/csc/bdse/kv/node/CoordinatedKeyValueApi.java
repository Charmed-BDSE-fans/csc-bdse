package ru.csc.bdse.kv.node;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CoordinatedKeyValueApi implements KeyValueApi {

    private final int rcl;
    private final int wcl;
    private final int timeout;
    private final List<InternalKeyValueApi> apis;

    public CoordinatedKeyValueApi(int rcl, int wcl, int timeout, List<InternalKeyValueApi> apis) {
        this.rcl = rcl;
        this.wcl = wcl;
        this.timeout = timeout;
        this.apis = apis;
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

        // FIXME: where is the ConflictResolver? O_o
        return results.get(0);
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
    public void delete(String key) { }

    @Override
    public Set<NodeInfo> getInfo() {
        return null;
    }

    @Override
    public void action(String node, NodeAction action) { }

    @Override
    public void deleteAll() { }
}
