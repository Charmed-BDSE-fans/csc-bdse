package ru.csc.bdse.kv;

import org.junit.Before;
import org.junit.Test;
import ru.csc.bdse.kv.node.*;
import ru.csc.bdse.util.Random;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public abstract class KeyValueApiHttpClientTest2 {
    private static final String KVNODE_NAME = "node-0";

    private KeyValueApi api = newKeyValueApi();

    protected abstract String kvnodeUrl();

    private KeyValueApi newKeyValueApi() {
        return new KeyValueApiHttpClient(kvnodeUrl());
    }

    @Before
    public void cleanDB() {
        api.action(KVNODE_NAME, NodeAction.UP);
        api.deleteAll();
    }

    @Test
    public void concurrentPuts() {
        // simultanious puts for the same key value

        final int CPU_NUM = Runtime.getRuntime().availableProcessors();
        final int MAX_VAL = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(CPU_NUM);

        for (int i = 0; i < CPU_NUM; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < MAX_VAL; j++) {
                    api.put("key", String.valueOf(j).getBytes());
                }
            });
        }

        executorService.shutdown();

        try {
            while (!executorService.awaitTermination(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        final Optional<byte[]> resp = api.get("key");

        assertTrue(resp.isPresent());
    }

    @Test
    public void concurrentDeleteAndKeys() {
        // simultanious delete by key and keys listing

        final int CPU_NUM = Runtime.getRuntime().availableProcessors();
        final int ELEMENTS_NUM = 1000;
        final int REMOVE_NUM = 500;

        ExecutorService executorService = Executors.newFixedThreadPool(CPU_NUM);

        byte[] data = new byte[] {1, 2, 3, 4};
        for (int i = 0; i < CPU_NUM * ELEMENTS_NUM; i++) {
            String key = Integer.toString(i);
            api.put(key, data);
        }

        final Set<String> removed = new ConcurrentSkipListSet<>();

        for (int i = 0; i < CPU_NUM; i++) {
            executorService.submit(() -> {
                java.util.Random rand = new java.util.Random();
                for (int j = 0; j < REMOVE_NUM; j++) {
                    String key = Integer.toString(rand.nextInt(CPU_NUM * ELEMENTS_NUM));
                    api.delete(key);
                    removed.add(key);
                }
            });
        }

        executorService.shutdown();

        try {
            while (!executorService.awaitTermination(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Set<String> remainingKeys = api.getKeys("");

        assertEquals(CPU_NUM * ELEMENTS_NUM - removed.size(), remainingKeys.size());
    }

    @Test
    public void actionUpDown() {
        // test up/down actions

        final Set<NodeInfo> ups = api.getInfo();
        for (NodeInfo info : ups) {
            if (Objects.equals(info.getName(), KVNODE_NAME)) {
                assertEquals(NodeStatus.UP, info.getStatus());
            }
        }

        api.action(KVNODE_NAME, NodeAction.DOWN);
        final Set<NodeInfo> downs = api.getInfo();
        for (NodeInfo info : downs) {
            if (Objects.equals(info.getName(), KVNODE_NAME)) {
                assertEquals(NodeStatus.DOWN, info.getStatus());
            }
        }
    }

    @Test
    public void putWithStoppedNode() {
        // test put if node/container was stopped

        final int ELEMENTS_NUM = 1000;

        String key = Random.nextKey();
        byte[] data = Random.nextValue();

        api.put(key, data);

        for (int i = 0; i < ELEMENTS_NUM; i++) {
            api.put(Random.nextKey(), Random.nextValue());
        }

        api.action(KVNODE_NAME, NodeAction.DOWN);

        Set<String> keys = api.getKeys("");
        assertEquals(0, keys.size());

        Optional<byte[]> respData = api.get(key);
        assertFalse(respData.isPresent());

        api.delete(key);

        api.action(KVNODE_NAME, NodeAction.UP);

        Optional<byte[]> respDataAfterDelete = api.get(key);
        assertTrue(respDataAfterDelete.isPresent());
        assertArrayEquals(data, respDataAfterDelete.get());
    }

    @Test
    public void getWithStoppedNode() {
        // test get if node/container was stopped

        final int ELEMENTS_NUM = 1000;

        String key = Random.nextKey();
        byte[] data = Random.nextValue();

        api.put(key, data);

        for (int i = 0; i < ELEMENTS_NUM; i++) {
            api.put(Random.nextKey(), Random.nextValue());
        }

        api.action(KVNODE_NAME, NodeAction.DOWN);

        Optional<byte[]> respData = api.get(key);
        assertFalse(respData.isPresent());
    }

    @Test
    public void getKeysByPrefixWithStoppedNode() {
        // test getKeysByPrefix if node/container was stopped

        final int ELEMENTS_NUM = 1000;

        for (int i = 0; i < ELEMENTS_NUM; i++) {
            api.put(Random.nextKey(), Random.nextValue());
        }

        api.action(KVNODE_NAME, NodeAction.DOWN);

        Set<String> keys = api.getKeys("");
        assertEquals(0, keys.size());
    }

    @Test
    public void deleteByTombstone() {
        // TODO use tombstones to mark as deleted (optional)
    }

    @Test
    public void loadMillionKeys() {
        //TODO load too many data (optional)
    }
}


