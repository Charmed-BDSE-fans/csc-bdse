package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import ru.csc.bdse.util.Env;
import ru.csc.bdse.util.Random;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class KeyValueApiHttpClientTest2 {

    @ClassRule
    public static final DockerComposeContainer composition =
            new DockerComposeContainer(new File("src/test/resources/kvnode/docker-compose.yml"))
                    .withEnv(Env.KVNODE_NAME, "node-0")
                    .withExposedService("kvnode", 8080);

    private KeyValueApi api = newKeyValueApi();

    private KeyValueApi newKeyValueApi() {
        final String baseUrl = "http://localhost:" + composition.getServicePort("kvnode", 8080);
        return new KeyValueApiHttpClient(baseUrl);
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
            executorService.wait();
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
            executorService.wait();
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
        //TODO test up/down actions
    }

    @Test
    public void putWithStoppedNode() {
        //TODO test put if node/container was stopped
    }

    @Test
    public void getWithStoppedNode() {
        //TODO test get if node/container was stopped
    }

    @Test
    public void getKeysByPrefixWithStoppedNode() {
        //TODO test getKeysByPrefix if node/container was stopped
    }

    @Test
    public void deleteByTombstone() {
        // TODO use tombstones to mark as deleted (optional)
    }

    @Test
    public void loadMillionKeys()  {
        //TODO load too many data (optional)
    }
}


