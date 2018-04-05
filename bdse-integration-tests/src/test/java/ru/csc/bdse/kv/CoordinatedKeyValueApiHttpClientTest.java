package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.Containers;

import java.util.Collections;

import static org.junit.Assume.assumeTrue;

public class CoordinatedKeyValueApiHttpClientTest extends AbstractKeyValueApiHttpClientTest {
    private static final Network testNetwork = Network.newNetwork();

    private static final Containers.KVNodeContainer kvnode = Containers
            .inMemoryNode(KVNODE_NAME)
            .withNetwork(testNetwork);

    static {
        Containers.coordinateKvNodes(Collections.singletonList(kvnode), 1, 1, 1000);
    }

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(kvnode);
    @Override
    protected String kvnodeUrl() {
        return kvnode.getKVBaseUrl(false);
    }

    @Override
    public void concurrentDeleteAndKeys() {
        /* Test expects that keys() method returns only keys that are alive;
           that is false... */
        //noinspection ConstantConditions
        assumeTrue(false);
        super.concurrentDeleteAndKeys();
    }
}
