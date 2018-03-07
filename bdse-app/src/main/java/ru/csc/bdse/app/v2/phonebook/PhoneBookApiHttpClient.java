package ru.csc.bdse.app.v2.phonebook;

import ru.csc.bdse.app.common.PhoneBookApiHttpClientBase;
import ru.csc.bdse.app.v2.controller.PhoneBookApiController;

public class PhoneBookApiHttpClient extends PhoneBookApiHttpClientBase<PhoneBookRecord> {
    public PhoneBookApiHttpClient(String baseUrl) {
        super(String.format("%s/%s/", baseUrl, PhoneBookApiController.URL_PREFIX), PhoneBookRecord.class);
    }
}
