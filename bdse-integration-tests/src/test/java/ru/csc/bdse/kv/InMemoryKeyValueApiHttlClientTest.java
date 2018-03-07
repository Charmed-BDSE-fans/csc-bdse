package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.Containers;

public class InMemoryKeyValueApiHttlClientTest extends KeyValueApiHttpClientTest2 {
    private static final String KVNODE_NAME = "node-0";
    private static final Network testNetwork = Network.newNetwork();

    private static final Containers.KVNodeContainer<?> kvnode = Containers
            .inMemoryNode(KVNODE_NAME)
            .withNetwork(testNetwork);

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(kvnode);

    @Override
    protected String kvnodeUrl() {
        return kvnode.getRESTBaseUrl(false);
    }
}