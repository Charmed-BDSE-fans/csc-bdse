package ru.csc.bdse.app.common;

import java.util.Set;

/**
 * Represents trivial phone book operations
 *
 * @author alesavin
 */
public interface PhoneBookApi<R extends Record> {

    /**
     * Put record
     */
    void put(R record);

    /**
     * Throw out record
     */
    void delete(R record);

    /**
     * Get all records associated with literal
     */
    Set<R> get(char literal);

    /**
     * Deletes everything
     */
    void deleteAll();
}