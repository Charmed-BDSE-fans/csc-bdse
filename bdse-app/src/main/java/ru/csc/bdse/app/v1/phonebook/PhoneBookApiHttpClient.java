package ru.csc.bdse.app.v1.phonebook;

import ru.csc.bdse.app.common.PhoneBookApiHttpClientBase;
import ru.csc.bdse.app.v1.controller.PhoneBookApiController;

public class PhoneBookApiHttpClient extends PhoneBookApiHttpClientBase<PhoneBookRecord> {
    public PhoneBookApiHttpClient(String baseUrl) {
        super(String.format("%s/%s/", baseUrl, PhoneBookApiController.URL_PREFIX), PhoneBookRecord.class);
    }
}
