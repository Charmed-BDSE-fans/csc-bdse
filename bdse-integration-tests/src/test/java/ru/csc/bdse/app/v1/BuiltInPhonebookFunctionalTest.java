package ru.csc.bdse.app.v1;

import ru.csc.bdse.app.AbstractPhoneBookFunctionalTest;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.KeyValuePhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.PhoneBookRecord;
import ru.csc.bdse.kv.node.InMemoryKeyValueApi;
import ru.csc.bdse.util.Random;

import java.util.Objects;

public class BuiltInPhonebookFunctionalTest extends AbstractPhoneBookFunctionalTest<PhoneBookRecord> {
    @Override
    protected PhoneBookRecord modifyContent(PhoneBookRecord record) {
        String newPhone = Random.randomString();
        return new PhoneBookRecord(record.getName(), record.getSurname(), newPhone);
    }

    @Override
    protected String getSurname(PhoneBookRecord record) {
        return record.getSurname();
    }

    @Override
    protected PhoneBookRecord modifySurname(PhoneBookRecord record) {
        String newSurname = "d" + Random.randomString();
        return new PhoneBookRecord(record.getName(), newSurname, record.getPhone());
    }

    @Override
    protected PhoneBookRecord randomRecord() {
        String name = "a" + Random.randomString();
        String surname = "b"+ Random.randomString();
        String phone = Random.randomString();
        return new PhoneBookRecord(name, surname, phone);
    }

    @Override
    protected boolean equalContent(PhoneBookRecord record1, PhoneBookRecord record2) {
        return Objects.equals(record1.getPhone(), record2.getPhone());
    }

    @Override
    protected PhoneBookApi<PhoneBookRecord> client() {
        return new KeyValuePhoneBookApi(new InMemoryKeyValueApi("111"));
    }
}
