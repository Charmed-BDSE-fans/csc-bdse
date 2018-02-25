package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.config.InMemoryKeyValueApiConfig;
import ru.csc.bdse.util.Env;
import ru.csc.bdse.util.Random;
import ru.csc.bdse.util.Containers;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class KeyValueApiHttpClientTest2 {
    private static final Network testNetwork = Network.newNetwork();
    private static final GenericContainer db = Containers.postgres(testNetwork);
    private static final GenericContainer kvnode = Containers.kvnode(testNetwork, InMemoryKeyValueApiConfig.PROFILE);

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvnode);

    private KeyValueApi api = newKeyValueApi();

    private KeyValueApi newKeyValueApi() {
        return new KeyValueApiHttpClient(Containers.getKVNodeBaseUrl(kvnode));
    }

    @Test
    public void concurrentPuts() {
        // simultanious puts for the same key value

        api.action(Env.KVNODE_NAME, NodeAction.UP);

        final String key = Random.nextKey();
        final ArrayList<byte[]> dataCandidates = new ArrayList<>();

        final int CPU_NUM = Runtime.getRuntime().availableProcessors();
        final int DATA_CANDIDATES_MAX = 1000;

        for (int i = 0; i < DATA_CANDIDATES_MAX * CPU_NUM; i++) {
            dataCandidates.add(Random.nextValue());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(CPU_NUM);

        for (int i = 0; i < CPU_NUM; i++) {
            final int taskID = i;
            executorService.submit(() -> {
                for (int j = 0; j < DATA_CANDIDATES_MAX; j++) {
                    int k = taskID * DATA_CANDIDATES_MAX + j;
                    api.put(key, dataCandidates.get(k));
                }
            });
        }

        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("can't wait the end of puts");
        }

        final Optional<byte[]> resp = api.get(key);

        assertTrue(resp.isPresent());

        final byte[] result = resp.get();

        for (int i = 0; i < CPU_NUM * DATA_CANDIDATES_MAX; i++) {
            if (Arrays.equals(result, dataCandidates.get(i))) {
                return;
            }
        }

        fail("get impossible data");
    }

    @Test
    public void concurrentDeleteAndKeys() {
        // simultanious delete by key and keys listing

        final int ELEMENTS_NUM = 100000;

        api.action(Env.KVNODE_NAME, NodeAction.UP);

        ExecutorService executorService = Executors.newCachedThreadPool();

        byte[] data = new byte[] {1, 2, 3, 4};
        for (int i = 0; i < ELEMENTS_NUM; i++) {
            String key = Integer.toString(i);
            api.put(key, data);
        }

        final Set<String> allKeys = api.getKeys("");
        assertEquals(ELEMENTS_NUM, allKeys.size());

        final List<String> removed = new ArrayList<>();

        final int REMOVE_NUM = ELEMENTS_NUM / 10;
        final int REMOVE_GROUPS_NUM = REMOVE_NUM / 10;

        for (int i = 0; i < REMOVE_GROUPS_NUM; i++) {
            executorService.submit(() -> {
                java.util.Random rand = new java.util.Random();
                for (int j = 0; j < REMOVE_NUM; j++) {
                    String key = Integer.toString(rand.nextInt() % ELEMENTS_NUM);
                    api.delete(key);
                    removed.add(key);
                }
            });
        }

        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("can't wait the end of tasks");
        }

        final Set<String> remainingKeys = api.getKeys("");

        assertEquals(ELEMENTS_NUM - REMOVE_NUM, remainingKeys.size());

        for (String key : remainingKeys) {
            for (String removedKey : removed) {
                assertNotEquals(key, removedKey);
            }
        }
    }

    @Test
    public void actionUpDown() {
        // test up/down actions

        api.action(Env.KVNODE_NAME, NodeAction.UP);
        final Set<NodeInfo> ups = api.getInfo();
        for (NodeInfo info : ups) {
            if (Objects.equals(info.getName(), Env.KVNODE_NAME)) {
                assertEquals(NodeStatus.UP, info.getStatus());
            }
        }

        api.action(Env.KVNODE_NAME, NodeAction.DOWN);
        final Set<NodeInfo> downs = api.getInfo();
        for (NodeInfo info : downs) {
            if (Objects.equals(info.getName(), Env.KVNODE_NAME)) {
                assertEquals(NodeStatus.DOWN, info.getStatus());
            }
        }
    }

    @Test
    public void putWithStoppedNode() {
        // test put if node/container was stopped

        final int ELEMENTS_NUM = 1000;

        api.action(Env.KVNODE_NAME, NodeAction.UP);

        String key = Random.nextKey();
        byte[] data = Random.nextValue();

        api.put(key, data);

        for (int i = 0; i < ELEMENTS_NUM; i++) {
            api.put(Random.nextKey(), Random.nextValue());
        }

        api.action(Env.KVNODE_NAME, NodeAction.DOWN);

        Set<String> keys = api.getKeys("");
        assertEquals(0, keys.size());

        Optional<byte[]> respData = api.get(key);
        assertFalse(respData.isPresent());

        api.delete(key);

        api.action(Env.KVNODE_NAME, NodeAction.UP);

        Optional<byte[]> respDataAfterDelete = api.get(key);
        assertTrue(respDataAfterDelete.isPresent());
        assertEquals(data, respDataAfterDelete.get());
    }

    @Test
    public void getWithStoppedNode() {
        // test get if node/container was stopped

        final int ELEMENTS_NUM = 1000;

        api.action(Env.KVNODE_NAME, NodeAction.UP);

        String key = Random.nextKey();
        byte[] data = Random.nextValue();

        api.put(key, data);

        for (int i = 0; i < ELEMENTS_NUM; i++) {
            api.put(Random.nextKey(), Random.nextValue());
        }

        api.action(Env.KVNODE_NAME, NodeAction.DOWN);

        Optional<byte[]> respData = api.get(key);
        assertFalse(respData.isPresent());
    }

    @Test
    public void getKeysByPrefixWithStoppedNode() {
        // test getKeysByPrefix if node/container was stopped

        final int ELEMENTS_NUM = 1000;

        api.action(Env.KVNODE_NAME, NodeAction.UP);

        for (int i = 0; i < ELEMENTS_NUM; i++) {
            api.put(Random.nextKey(), Random.nextValue());
        }

        api.action(Env.KVNODE_NAME, NodeAction.DOWN);

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


