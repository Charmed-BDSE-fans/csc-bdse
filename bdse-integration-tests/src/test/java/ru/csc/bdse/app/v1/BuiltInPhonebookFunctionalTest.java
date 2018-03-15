package ru.csc.bdse.app.v1;

import ru.csc.bdse.app.AbstractPhoneBookFunctionalTest;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.KeyValuePhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.PhoneBookRecord;
import ru.csc.bdse.kv.node.InMemoryKeyValueApi;
import ru.csc.bdse.util.Random;

public class BuiltInPhonebookFunctionalTest extends AbstractPhoneBookFunctionalTest<PhoneBookRecord> {
    @Override
    protected PhoneBookRecord modifyContent(PhoneBookRecord record) {
        String newPhone = Random.randomString();
        return new PhoneBookRecord(record.getName(), record.getSurname(), newPhone);
    }

    @Override
    protected PhoneBookRecord randomRecord() {
        String name = Random.randomString();
        String surname = Random.randomString();
        String phone = Random.randomString();
        return new PhoneBookRecord(name, surname, phone);
    }


    @Override
    protected PhoneBookApi<PhoneBookRecord> client() {
        return new KeyValuePhoneBookApi(new InMemoryKeyValueApi("111"));
    }
}
