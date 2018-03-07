package ru.csc.bdse.app.v1.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.app.Application;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.common.PhoneBookApiControllerBase;
import ru.csc.bdse.app.v1.phonebook.PhoneBookRecord;

@Profile(Application.V1_PROFILE)
@RestController
@RequestMapping(PhoneBookApiController.URL_PREFIX)
public class PhoneBookApiController extends PhoneBookApiControllerBase<PhoneBookRecord> {
    public static final String URL_PREFIX = "/v1/";

    public PhoneBookApiController(PhoneBookApi<PhoneBookRecord> api) {
        super(api);
    }
}
