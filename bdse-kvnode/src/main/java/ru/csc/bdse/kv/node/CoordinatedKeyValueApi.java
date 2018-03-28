package ru.csc.bdse.kv.node;

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
                    return task.get(timeout, TimeUnit.SECONDS);
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
        return null;
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return null;
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
