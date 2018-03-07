package ru.csc.bdse.app.v2.contorller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import ru.csc.bdse.app.Application;
import ru.csc.bdse.app.base.PhoneBookApi;
import ru.csc.bdse.app.v2.phonebook.PhoneBookRecord;

import java.util.Set;

@RestController
@Profile(Application.V2_PROFILE)
public class PhoneBookApiController implements PhoneBookApi<PhoneBookRecord> {

    private final PhoneBookApi<PhoneBookRecord> api;

    public PhoneBookApiController(PhoneBookApi<PhoneBookRecord> api) {
        this.api = api;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/phonebook")
    @Override
    public void put(@RequestBody PhoneBookRecord record) {
        api.put(record);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/phonebook/")
    @Override
    public void delete(@RequestBody PhoneBookRecord record) {
        api.delete(record);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/phonebook/{literal}")
    @Override
    public Set<PhoneBookRecord> get(@PathVariable char literal) {
        return api.get(literal);
    }
}
