package ru.csc.bdse.app.v1.contorller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import ru.csc.bdse.app.Application;
import ru.csc.bdse.app.v1.phonebook.PhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.Record;

import java.util.Set;

@RestController
@Profile(Application.V1_PROFILE)
public class PhoneBookApiController implements PhoneBookApi<Record> {

    private final PhoneBookApi<Record> api;

    public PhoneBookApiController(PhoneBookApi<Record> api) {
        this.api = api;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/phonebook")
    @Override
    public void put(@RequestBody Record record) {
        api.put(record);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/phonebook/")
    @Override
    public void delete(@RequestBody Record record) {
        api.delete(record);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/phonebook/{literal}")
    @Override
    public Set<Record> get(@PathVariable char literal) {
        return null;
    }
}
