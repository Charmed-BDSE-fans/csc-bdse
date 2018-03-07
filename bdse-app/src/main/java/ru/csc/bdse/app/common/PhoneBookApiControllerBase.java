package ru.csc.bdse.app.common;

import org.springframework.web.bind.annotation.*;

import java.util.Set;

public class PhoneBookApiControllerBase<R extends Record> implements PhoneBookApi<R> {
    private final PhoneBookApi<R> api;

    public PhoneBookApiControllerBase(PhoneBookApi<R> api) {
        this.api = api;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/phonebook")
    @Override
    public void put(@RequestBody R record) {
        api.put(record);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/phonebook")
    @Override
    public void delete(@RequestBody R record) {
        api.delete(record);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/phonebook/{literal}")
    @Override
    public Set<R> get(@PathVariable char literal) {
        return api.get(literal);
    }
}
