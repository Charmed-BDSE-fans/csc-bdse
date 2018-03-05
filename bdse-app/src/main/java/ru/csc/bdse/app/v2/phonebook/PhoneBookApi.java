package ru.csc.bdse.app.v2.phonebook;

import ru.csc.bdse.app.v1.phonebook.Record;

import java.util.Set;


/**
 * Represents trivial phone book operations
 *
 * @author alesavin
 */
public interface PhoneBookApi<R extends Record> extends ru.csc.bdse.app.v1.phonebook.PhoneBookApi {
    /**
     * Get all records associated with nickname literal
     */
    Set<R> getByNickname(char literal);
}