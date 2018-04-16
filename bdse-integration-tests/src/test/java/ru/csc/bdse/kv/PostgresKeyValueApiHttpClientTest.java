package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.Containers;

public class PostgresKeyValueApiHttpClientTest extends AbstractKeyValueApiHttpClientTest {
    private static final Network testNetwork = Network.newNetwork();
    private static final Containers.PostgresContainer db = Containers
            .postgresDB()
            .withNetwork(testNetwork);
    private static final Containers.KVNodeContainer kvnode = Containers
            .postgresNode(KVNODE_NAME, db.getConnectionUrl(true))
            .withNetwork(testNetwork);

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvnode);

    @Override
    protected String kvnodeUrl() {
        return kvnode.getKVBaseUrl(false);
    }
}
