package ru.csc.bdse.app.v2;

import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.AbstractPhoneBookFunctionalTest;
import ru.csc.bdse.app.v2.phonebook.PhoneBookApiHttpClient;
import ru.csc.bdse.app.v2.phonebook.PhoneBookRecord;
import ru.csc.bdse.util.Containers;
import ru.csc.bdse.util.Containers.AppContainer.Version;
import ru.csc.bdse.util.Random;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhoneBookFunctionalTest extends AbstractPhoneBookFunctionalTest<PhoneBookRecord> {
    private static final Network testNetwork = Network.newNetwork();
    private static final Containers.PostgresContainer db = Containers
            .postgresDB()
            .withNetwork(testNetwork);

    private static final Containers.KVNodeContainer kvnode = Containers
            .postgresNode(db.getConnectionUrl(true))
            .withNetwork(testNetwork);

    private static final Containers.AppContainer app = Containers
            .applicationWithRemoteKV(Version.V2, kvnode.getRESTBaseUrl(true))
            .withNetwork(testNetwork);

    @ClassRule
    public static final RuleChain ruleChain =
            RuleChain.outerRule(testNetwork)
                    .around(db)
                    .around(kvnode)
                    .around(app);

    @Override
    protected PhoneBookRecord modifyContent(PhoneBookRecord record) {
        String p = Random.randomString();
        List<String> phones = record.getPhones().stream()
                .filter(o -> Random.randomBool())
                .collect(Collectors.toList());

        phones.add(p);

        return new PhoneBookRecord(record.getName(), record.getSurname(), record.getNickname(), phones);
    }

    @Override
    protected String getSurname(PhoneBookRecord record) {
        return record.getSurname();
    }

    @Override
    protected PhoneBookRecord modifySurname(PhoneBookRecord record) {
        String newSurname = "d" + Random.randomString();
        return new PhoneBookRecord(record.getName(), newSurname, record.getNickname(), record.getPhones());
    }

    @Override
    protected boolean equalContent(PhoneBookRecord record1, PhoneBookRecord record2) {
        return Objects.equals(record1.getPhone(), record2.getPhone()) &&
                Objects.equals(record1.getPhones(), record2.getPhones());
    }

    @Override
    protected PhoneBookRecord randomRecord() {
        String name = "a" + Random.randomString();
        String surname = "b" + Random.randomString();
        String nickname = "c" + Random.randomString();
        List<String> phones = Stream.generate(Random::nextKey)
                .limit(Random.randomInt(10) + 1)
                .collect(Collectors.toList());

        return new PhoneBookRecord(name, surname, nickname, phones);
    }

    @Override
    protected PhoneBookApiHttpClient client() {
        return new PhoneBookApiHttpClient(app.getRESTBaseUrl(false));
    }
}