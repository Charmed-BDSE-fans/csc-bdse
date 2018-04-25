package ru.csc.bdse.kv;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import ru.csc.bdse.kv.node.KeyValueApi;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public abstract class AbstractPartitionedKeyValueApiHttpClientTest {

    protected abstract KeyValueApi newCluster1();
    protected abstract KeyValueApi newCluster2();

    protected Set<String> randKeys =
            Stream.generate(() -> RandomStringUtils.randomAlphanumeric(10)).limit(1000).collect(Collectors.toSet());

    protected Set<String> keys() {
        if(cluster1 == null && cluster2 == null) {
            return new HashSet<>();
        }
        if(cluster1 != null) {
            return cluster1.getKeys("");
        }
        return cluster2.getKeys("");
    }

    protected abstract float expectedKeysLossProportion();
    protected abstract float expectedUndeletedKeysProportion();

    private KeyValueApi cluster1 = newCluster1();
    private KeyValueApi cluster2 = newCluster2();
    private Set<String> keys = keys();

    @Test
    public void put1000KeysAndReadItCorrectlyOnCluster1() {
        for(String key: randKeys) {
            cluster1.put(key, key.getBytes());
        }
        for(String key: randKeys) {
            assertTrue(cluster1.get(key).isPresent());
        }
    }

    @Test
    public void readKeysFromCluster2AndCheckLossProportion() {
        double succ = 0.0;
        double total = 0.0;
        for(String key: randKeys) {
            if(cluster2.get(key).isPresent()) {
                succ += 1;
            }
            total += 1;
        }
        assertTrue((succ / total) <= expectedKeysLossProportion());
    }

    @Test
    public void deleteAllKeysFromCluster2() {
        // TODO try to delete all keys on cluster2
        for(String key: randKeys) {
            cluster2.delete(key);
        }
    }

    @Test
    public void readKeysFromCluster1AfterDeletionAtCluster2() {
        // TODO read all keys from cluster1, made some statistics (related to expectedUndeletedKeysProportion)
        Set<String> keys = cluster1.getKeys("");
        assertTrue((float) keys.size() / randKeys.size() <= expectedUndeletedKeysProportion());
    }
}


