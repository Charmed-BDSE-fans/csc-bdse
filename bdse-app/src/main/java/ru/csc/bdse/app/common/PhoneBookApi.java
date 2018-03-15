package ru.csc.bdse.app.common;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
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
}