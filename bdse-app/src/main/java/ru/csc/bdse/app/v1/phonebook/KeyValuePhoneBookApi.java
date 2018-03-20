package ru.csc.bdse.app.v1.phonebook;

import ru.csc.bdse.app.common.KeyValuePhoneBookApiBase;
import ru.csc.bdse.kv.node.KeyValueApi;

public class KeyValuePhoneBookApi extends KeyValuePhoneBookApiBase<PhoneBookRecord> {
    public KeyValuePhoneBookApi(KeyValueApi kva) {
        super(kva, PhoneBookRecord.class);
    }

    @Override
    protected String getId(PhoneBookRecord record) {
        return String.format("%s-%s", record.getName(), record.getSurname());
    }
}
