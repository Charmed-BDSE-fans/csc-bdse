package ru.csc.bdse.app.v2.phonebook;

import ru.csc.bdse.app.v1.phonebook.Record;

import java.util.Set;

public class PhoneBookApiHttpClient implements PhoneBookApi<Record> {

    @Override
    public void put(Record record) {
        // TODO
    }

    @Override
    public void delete(Record record) {
        // TODO
    }

    @Override
    public Set<Record> get(char literal) {
        // TODO
        return null;
    }

    @Override
    public Set<Record> getByNickname(char literal) {
        // TODO
        return null;
    }
}
