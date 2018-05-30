package ru.csc.bdse.kv.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.csc.bdse.partitioning.Partitioner;

import java.util.*;
import java.util.concurrent.*;

public class PartitioningKeyValueApi implements KeyValueApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueApiHttpClient.class);
    private final Partitioner partitioner;
    private final long timeout;
    private final Map<String, KeyValueApi> shards;
    private final ExecutorService executorService;

    public PartitioningKeyValueApi(Map<String, KeyValueApi> shards, long timeout, Partitioner partitioner) {
        this.shards = shards;
        this.timeout = timeout;
        this.partitioner = partitioner;
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void put(String key, byte[] value) {
        Future<?> f = executorService.submit(() -> shards.get(partitioner.getPartition(key)).put(key, value));

        try {
            f.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.info(e.getMessage());
            throw new RuntimeException("something went wrong", e);
        }
    }

    @Override
    public Optional<byte[]> get(String key) {
        Future<Optional<byte[]>> f = executorService.submit(() -> shards.get(partitioner.getPartition(key)).get(key));

        Optional<byte[]> res = Optional.empty();

        try {
            res = f.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.info(e.getMessage());
            throw new RuntimeException("something went wrong", e);
        }

        return res;
    }

    @Override
    public Set<String> getKeys(String prefix) {
        List<Future<Set<String>>> fkeys = new ArrayList<>();

        for (KeyValueApi s : shards.values()) {
            fkeys.add(executorService.submit(() -> s.getKeys(prefix)));
        }

        Set<String> keys = new HashSet<>();

        for (Future<Set<String>> fs : fkeys) {
            try {
                keys.addAll(fs.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.info(e.getMessage());
                throw new RuntimeException("something went wrong", e);
            }
        }

        return keys;
    }

    @Override
    public void delete(String key) {
        Future<?> f = executorService.submit(() -> shards.get(partitioner.getPartition(key)).delete(key));

        try {
            f.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.info(e.getMessage());
            throw new RuntimeException("something went wrong", e);
        }
    }

    @Override
    public Set<NodeInfo> getInfo() {
        List<Future<Set<NodeInfo>>> finfos = new ArrayList<>();

        for (KeyValueApi s : shards.values()) {
            finfos.add(executorService.submit(s::getInfo));
        }

        Set<NodeInfo> infos = new HashSet<>();

        for (Future<Set<NodeInfo>> i : finfos) {
            try {
                infos.addAll(i.get(timeout, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.info(e.getMessage());
                throw new RuntimeException("something went wrong", e);
            }
        }

        return infos;
    }

    @Override
    public void action(String node, NodeAction action) {
        Future<?> f = executorService.submit(() -> shards.get(partitioner.getPartition(node)).action(node, action));

        try {
            f.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.info(e.getMessage());
            throw new RuntimeException("something went wrong", e);
        }
    }

    @Override
    public void deleteAll() {
        for (KeyValueApi s : shards.values()) {
            s.deleteAll();
        }
    }
}
