package ru.csc.bdse.app.common;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;

public class PhoneBookApiControllerBase<R extends Record> {
    private final PhoneBookApi<R> api;

    public PhoneBookApiControllerBase(PhoneBookApi<R> api) {
        this.api = api;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/phonebook")
    public void put(@RequestBody R record) {
        api.put(record);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/phonebook")
    public void delete(@RequestBody R record) {
        api.delete(record);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/phonebook/{literal}")
    public Set<R> get(@PathVariable char literal) {
        return api.get(literal);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteAll")
    public void deleteAll() {
        api.deleteAll();
    }
}
