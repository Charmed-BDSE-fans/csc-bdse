package ru.csc.bdse.app.v2.phonebook;

import ru.csc.bdse.app.common.PhoneBookApiClientBase;
import ru.csc.bdse.kv.KeyValueApi;

public class PhoneBookApiClient extends PhoneBookApiClientBase<PhoneBookRecord> {
    public PhoneBookApiClient(KeyValueApi kva) {
        super(kva, PhoneBookRecord.class);
    }
}
