package ru.csc.bdse.app.v2.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.common.PhoneBookApiControllerBase;
import ru.csc.bdse.app.v2.phonebook.PhoneBookRecord;

@RestController
@RequestMapping(PhoneBookApiController.URL_PREFIX)
public class PhoneBookApiController extends PhoneBookApiControllerBase<PhoneBookRecord> {
    public static final String URL_PREFIX = "/v2/";

    public PhoneBookApiController(PhoneBookApi<PhoneBookRecord> api) {
        super(api);
    }
}
