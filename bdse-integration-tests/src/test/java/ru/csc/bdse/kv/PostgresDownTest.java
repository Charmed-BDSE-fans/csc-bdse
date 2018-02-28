package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.Containers;
import ru.csc.bdse.util.Random;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostgresDownTest {
    private static final String KVNODE_NAME = "node-0";
    private static final Network testNetwork = Network.newNetwork();
    private static final Containers.PostgresContainer<?> db = Containers
            .postgresDB()
            .withNetwork(testNetwork);
    private static final Containers.KVNodeContainer<?> kvnode = Containers
            .postgresNode(KVNODE_NAME, db.getConnectionUrl(true))
            .withNetwork(testNetwork);

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvnode);

    private String kvnodeUrl() {
        return kvnode.getRESTBaseUrl(false);
    }

    @Test (expected = org.springframework.web.client.HttpServerErrorException.class)
    public void dbDown() {
        KeyValueApi api = new KeyValueApiHttpClient(kvnodeUrl());

        final int ELEMENTS_NUM = 1000;

        api.action(KVNODE_NAME, NodeAction.UP);

        String key = Random.nextKey();
        byte[] data = Random.nextValue();
        api.put(key, data);
        for (int i = 0; i < ELEMENTS_NUM; i++) {
            api.put(Random.nextKey(), Random.nextValue());
        }

        db.stop();

        api.put(Random.nextKey(), Random.nextValue());
    }
}
