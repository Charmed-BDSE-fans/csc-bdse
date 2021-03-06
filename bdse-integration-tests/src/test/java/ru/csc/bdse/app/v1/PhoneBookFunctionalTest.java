package ru.csc.bdse.app.v1;

import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.AbstractPhoneBookFunctionalTest;
import ru.csc.bdse.app.v1.phonebook.PhoneBookApiHttpClient;
import ru.csc.bdse.app.v1.phonebook.PhoneBookRecord;
import ru.csc.bdse.util.Containers;
import ru.csc.bdse.util.Containers.AppContainer.Version;
import ru.csc.bdse.util.Random;

import java.util.Objects;

public class PhoneBookFunctionalTest extends AbstractPhoneBookFunctionalTest<PhoneBookRecord> {
    private static final Network testNetwork = Network.newNetwork();
    private static final Containers.PostgresContainer db = Containers
            .postgresDB()
            .withNetwork(testNetwork);

    private static final Containers.KVNodeContainer kvnode = Containers
            .postgresNode(db.getConnectionUrl(true))
            .withNetwork(testNetwork);

    private static final Containers.AppContainer app = Containers
            .applicationWithRemoteKV(Version.V1, kvnode.getKVBaseUrl(true))
            .withNetwork(testNetwork);


    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvnode)
                    .around(app);


    @Override
    protected PhoneBookRecord modifyContent(PhoneBookRecord record) {
        String newPhone = Random.randomString();
        return new PhoneBookRecord(record.getName(), record.getSurname(), newPhone);
    }

    @Override
    protected PhoneBookRecord modifySurname(PhoneBookRecord record) {
        String newSurname = "d" + Random.randomString();

        return new PhoneBookRecord(record.getName(), newSurname, record.getPhone());
    }

    @Override
    protected String getSurname(PhoneBookRecord record) {
        return record.getSurname();
    }

    @Override
    protected PhoneBookRecord randomRecord() {
        String name = "a" + Random.randomString();
        String surname = "b" + Random.randomString();
        String phone = Random.randomString();
        return new PhoneBookRecord(name, surname, phone);
    }

    @Override
    protected boolean equalContent(PhoneBookRecord record1, PhoneBookRecord record2) {
        return Objects.equals(record1.getPhone(), record2.getPhone());
    }

    @Override
    protected PhoneBookApiHttpClient client() {
        return new PhoneBookApiHttpClient(app.getAppBaseUrl(false));
    }
}