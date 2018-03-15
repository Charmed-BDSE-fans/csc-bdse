package ru.csc.bdse.app;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.util.Containers;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class PhoneBookCompatibilitiesTest {
    private static final Network testNetwork = Network.newNetwork();

    private static final Containers.PostgresContainer db = Containers
            .postgresDB()
            .withNetwork(testNetwork);

    private static final Containers.KVNodeContainer kvNode = Containers
            .postgresNode(db.getConnectionUrl(true))
            .withNetwork(testNetwork);

    private static final Containers.AppContainer appV1 = Containers
            .applicationWithRemoteKV(Containers.AppContainer.Version.V1, kvNode.getRESTBaseUrl(true))
            .withNetwork(testNetwork);

    private static final Containers.AppContainer appV2 = Containers
            .applicationWithRemoteKV(Containers.AppContainer.Version.V2, kvNode.getRESTBaseUrl(true))
            .withNetwork(testNetwork);

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvNode)
                    .around(appV1)
                    .around(appV2);

    private ru.csc.bdse.app.v1.phonebook.PhoneBookApiHttpClient clientV1() {
        return new ru.csc.bdse.app.v1.phonebook.PhoneBookApiHttpClient(appV1.getRESTBaseUrl(false));
    }

    private ru.csc.bdse.app.v2.phonebook.PhoneBookApiHttpClient clientV2() {
        return new ru.csc.bdse.app.v2.phonebook.PhoneBookApiHttpClient(appV2.getRESTBaseUrl(false));
    }

    private PhoneBookApi<ru.csc.bdse.app.v1.phonebook.PhoneBookRecord> apiV1;
    private PhoneBookApi<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> apiV2;

    private synchronized PhoneBookApi<ru.csc.bdse.app.v1.phonebook.PhoneBookRecord> getPhoneBookApiV1() {
        if (apiV1 == null) {
            apiV1 = clientV1();
        }
        return apiV1;
    }

    private synchronized PhoneBookApi<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> getPhoneBookApiV2() {
        if (apiV2 == null) {
            apiV2 = clientV2();
        }
        return apiV2;
    }

    @Before
    public void cleanPB() {
        getPhoneBookApiV1().deleteAll();
        getPhoneBookApiV2().deleteAll();
    }

    @Test
    public void write10read11() {
        ru.csc.bdse.app.v1.phonebook.PhoneBookRecord original
                = new ru.csc.bdse.app.v1.phonebook.PhoneBookRecord("aba", "caba", "+apple");
        getPhoneBookApiV1().put(original);
        Set<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> records = getPhoneBookApiV2().get(original.getSurname().charAt(0));
        assertEquals(1, records.size());

        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord actual = records.stream().findFirst().get();
        assertEquals(original.getId(), actual.getId());
        assertEquals(original.getName(), actual.getName());
        assertEquals(original.getSurname(), actual.getSurname());
        assertEquals(original.getPhone(), actual.getPhone());

        assertNull(actual.getNickname());
        assertArrayEquals(new String[] {original.getPhone()}, actual.getPhones().toArray());
    }

    @Test
    public void write11read10() {
        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord original
                = new ru.csc.bdse.app.v2.phonebook.PhoneBookRecord(
                        "aba", "caba", "daba", Arrays.asList("+apple", "+peanut")
        );
        getPhoneBookApiV2().put(original);
        Set<ru.csc.bdse.app.v1.phonebook.PhoneBookRecord> records = getPhoneBookApiV1().get(original.getSurname().charAt(0));
        assertEquals(1, records.size());

        ru.csc.bdse.app.v1.phonebook.PhoneBookRecord actual = records.stream().findFirst().get();
        assertEquals(original.getId(), actual.getId());
        assertEquals(original.getName(), actual.getName());
        assertEquals(original.getSurname(), actual.getSurname());
        assertEquals(original.getPhone(), actual.getPhone());
    }

    @Test
    public void write10erasure11() {
        ru.csc.bdse.app.v1.phonebook.PhoneBookRecord original
                = new ru.csc.bdse.app.v1.phonebook.PhoneBookRecord("aba", "caba", "+apple");
        getPhoneBookApiV1().put(original);
        Set<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> records = getPhoneBookApiV2().get(original.getSurname().charAt(0));
        assertEquals(1, records.size());

        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord original2 = records.stream().findFirst().get();
        getPhoneBookApiV2().delete(original2);

        assertTrue(getPhoneBookApiV1().get(original.getSurname().charAt(0)).isEmpty());
    }
}
