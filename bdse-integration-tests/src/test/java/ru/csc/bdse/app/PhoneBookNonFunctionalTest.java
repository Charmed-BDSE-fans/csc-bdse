package ru.csc.bdse.app;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.WaitAllStrategy;
import org.testcontainers.containers.wait.WaitStrategy;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.PhoneBookRecord;
import ru.csc.bdse.app.v2.phonebook.PhoneBookApiHttpClient;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApiHttpClient;
import ru.csc.bdse.kv.node.NodeAction;
import ru.csc.bdse.util.Containers;
import ru.csc.bdse.util.Random;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class PhoneBookNonFunctionalTest {
    private static final String KVNODE_NAME = "node-0";
    private static final Network testNetwork = Network.newNetwork();
    private static final Containers.PostgresContainer db = Containers
            .postgresDB()
            .withNetwork(testNetwork);

    private static final Containers.KVNodeContainer kvnode = Containers
            .postgresNode(KVNODE_NAME, db.getConnectionUrl(true))
            .withNetwork(testNetwork);

    private static final Containers.AppContainer app1 = Containers
            .applicationWithRemoteKV(Containers.AppContainer.Version.V1, kvnode.getRESTBaseUrl(true))
            .withNetwork(testNetwork);

    private static final Containers.AppContainer app2 = Containers
            .applicationWithRemoteKV(Containers.AppContainer.Version.V2, kvnode.getRESTBaseUrl(true))
            .withNetwork(testNetwork);

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvnode)
                    .around(app1)
                    .around(app2);


    private KeyValueApi kvapi = getKvapi();
    private PhoneBookApi<PhoneBookRecord> app1api = getApp1Api();
    private PhoneBookApi<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> app2api = getApp2Api();

    private KeyValueApi getKvapi() {
        return new KeyValueApiHttpClient(kvnode.getRESTBaseUrl(false));
    }

    private PhoneBookApi<PhoneBookRecord>  getApp1Api() {
        return new  ru.csc.bdse.app.v1.phonebook.PhoneBookApiHttpClient(app1.getRESTBaseUrl(false));
    }

    private PhoneBookApi<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> getApp2Api() {
        return new PhoneBookApiHttpClient(app2.getRESTBaseUrl(false));
    }

    @Test
    public void putGetErasureWithStoppedNode() {
        // test put, get, erasure if kv-node container was broken

        kvapi.action(KVNODE_NAME, NodeAction.DOWN);

        PhoneBookRecord record1 = new PhoneBookRecord(Random.randomString(), Random.randomString(), Random.randomString());
        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord record2 = new ru.csc.bdse.app.v2.phonebook.PhoneBookRecord(
                Random.randomString(),
                Random.randomString(),
                Random.randomString(),
                Arrays.asList(Random.randomString()));


        app1api.put(record1);
        app2api.put(record2);

        assertTrue(app1api.get(record1.literals().stream().findFirst().get()).isEmpty());
        assertTrue(app2api.get(record2.literals().stream().findFirst().get()).isEmpty());

        app1api.delete(record1);
        app1api.delete(record2);
    }

    @Test
    public void dataWasSavedIfAppRestarts() {
        // test data was saved after app restarts

        PhoneBookRecord record1 = new PhoneBookRecord(Random.randomString(), Random.randomString(), Random.randomString());
        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord record2 = new ru.csc.bdse.app.v2.phonebook.PhoneBookRecord(
                Random.randomString(),
                Random.randomString(),
                Random.randomString(),
                Arrays.asList(Random.randomString()));


        app1api.put(record1);
        app2api.put(record2);

        app1.close();
        app2.close();

        app1.start();
        app2.start();

        WaitStrategy ws1 = new WaitAllStrategy();
        ws1.waitUntilReady(app1);
        app1.waitingFor(ws1);

        WaitStrategy ws2 = new WaitAllStrategy();
        ws2.waitUntilReady(app2);
        app2.waitingFor(ws2);

        app1api = getApp1Api();
        app2api = getApp2Api();

        assertEquals(record1, app1api.get(record1.literals().stream().findFirst().get()).stream().findFirst().get());
        assertEquals(record2, app2api.get(record2.literals().stream().findFirst().get()).stream().findFirst().get());
    }

    @Test
    public void dataWasSavedIfKvNodeRestarts() {
        // test data was saved after kv-node restarts

        PhoneBookRecord record1 = new PhoneBookRecord(Random.randomString(), Random.randomString(), Random.randomString());
        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord record2 = new ru.csc.bdse.app.v2.phonebook.PhoneBookRecord(
                Random.randomString(),
                Random.randomString(),
                Random.randomString(),
                Arrays.asList(Random.randomString()));

        app1api.put(record1);
        app2api.put(record2);

        kvapi.action(KVNODE_NAME, NodeAction.DOWN);
        kvapi.action(KVNODE_NAME, NodeAction.UP);

        assertEquals(record1, app1api.get(record1.literals().stream().findFirst().get()).stream().findFirst().get());
        assertEquals(record2, app2api.get(record2.literals().stream().findFirst().get()).stream().findFirst().get());
    }
}