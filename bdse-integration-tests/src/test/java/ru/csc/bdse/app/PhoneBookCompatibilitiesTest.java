package ru.csc.bdse.app;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.util.Containers;
import ru.csc.bdse.util.Random;
import ru.csc.bdse.app.common.PhoneBookApi;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class PhoneBookCompatibilitiesTest {
    private static final String KVNODE_NAME_0 = "node-10";
    private static final String KVNODE_NAME_1 = "node-11";

    private static final Network testNetwork = Network.newNetwork();
    private static final Containers.PostgresContainer db = Containers
            .postgresDB()
            .withNetwork(testNetwork);

    private static final Containers.KVNodeContainer kvnode0 = Containers
            .postgresNode(KVNODE_NAME_0, db.getConnectionUrl(true))
            .withNetwork(testNetwork);
    private static final Containers.KVNodeContainer kvnode1 = Containers
            .postgresNode(KVNODE_NAME_1, db.getConnectionUrl(true))
            .withNetwork(testNetwork);

    private static final Containers.AppContainer app0 = Containers
            .applicationWithRemoteKV(Containers.AppContainer.Version.V1, kvnode0.getRESTBaseUrl(true))
            .withNetwork(testNetwork);
    private static final Containers.AppContainer app1 = Containers
            .applicationWithRemoteKV(Containers.AppContainer.Version.V2, kvnode1.getRESTBaseUrl(true))
            .withNetwork(testNetwork);

    private PhoneBookApi<ru.csc.bdse.app.v1.phonebook.PhoneBookRecord> api0 = null;
    private PhoneBookApi<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> api1 = null;

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvnode0)
                    .around(kvnode1)
                    .around(app0)
                    .around(app1);

//    @Override
//    protected PhoneBookRecord modifyContent(PhoneBookRecord record) {
//        String newPhone = Random.randomString();
//        return new PhoneBookRecord(record.getName(), record.getSurname(), newPhone);
//    }

//    @Override
//    protected PhoneBookRecord modifyContent(PhoneBookRecord record) {
//        String p = Random.randomString();
//        List<String> phones = record.getPhones().stream()
//                .filter(o -> Random.randomBool())
//                .collect(Collectors.toList());
//
//        phones.add(p);
//
//        return new PhoneBookRecord(record.getName(), record.getNickname(), record.getSurname(), phones);
//    }

    protected ru.csc.bdse.app.v2.phonebook.PhoneBookRecord randomRecordV2() {
        String name = Random.randomString();
        String surname = Random.randomString();
        String nickname = Random.randomString();
        List<String> phones = Stream.generate(Random::nextKey)
                .limit(Random.randomInt(10) + 1)
                .collect(Collectors.toList());

        return new ru.csc.bdse.app.v2.phonebook.PhoneBookRecord(name, surname, nickname, phones);
    }

    protected ru.csc.bdse.app.v1.phonebook.PhoneBookRecord randomRecordV1() {
        String name = Random.randomString();
        String surname = Random.randomString();
        String phone = Random.randomString();
        return new ru.csc.bdse.app.v1.phonebook.PhoneBookRecord(name, surname, phone);
    }


    protected ru.csc.bdse.app.v1.phonebook.PhoneBookApiHttpClient clientV1() {
        return new ru.csc.bdse.app.v1.phonebook.PhoneBookApiHttpClient(app0.getRESTBaseUrl(false));
    }

    protected ru.csc.bdse.app.v2.phonebook.PhoneBookApiHttpClient clientV2() {
        return new ru.csc.bdse.app.v2.phonebook.PhoneBookApiHttpClient(app1.getRESTBaseUrl(false));
    }

    private synchronized PhoneBookApi<ru.csc.bdse.app.v1.phonebook.PhoneBookRecord> getPhoneBookApiV1() {
        if (api0 == null) {
            api0 = clientV1();
        }
        return api0;
    }

    private synchronized PhoneBookApi<ru.csc.bdse.app.v2.phonebook.PhoneBookRecord> getPhoneBookApiV2() {
        if (api1 == null) {
            api1 = clientV2();
        }
        return api1;
    }

    @Before
    public void cleanPB() {
        getPhoneBookApiV1().deleteAll();
        getPhoneBookApiV2().deleteAll();
    }

    @Test
    public void write10read11() {
        //TODO write data in book 1.0 format and read in book 1.1
    }

    @Test
    public void write11read10() {
        // TODO write data in book 1.1 format and read in book 1.0
    }

    @Test
    public void write10erasure11() {
        // TODO write data in book 1.0 format and erasure in book 1.1
    }
}
