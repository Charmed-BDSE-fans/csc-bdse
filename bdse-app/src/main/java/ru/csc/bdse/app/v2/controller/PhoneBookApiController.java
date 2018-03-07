package ru.csc.bdse.app.v2.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.app.base.PhoneBookApi;
import ru.csc.bdse.app.base.PhoneBookApiControllerBase;
import ru.csc.bdse.app.v2.phonebook.PhoneBookRecord;

@RestController
@RequestMapping("/v2/")
public class PhoneBookApiController extends PhoneBookApiControllerBase<PhoneBookRecord> {
    public PhoneBookApiController(PhoneBookApi<PhoneBookRecord> api) {
        super(api);
    }
}
