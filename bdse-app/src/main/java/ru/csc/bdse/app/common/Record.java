package ru.csc.bdse.app.common;

import java.util.Set;

/**
 * Phone book record
 *
 * @author alesavin
 */
public interface Record {
    /**
     * Returns record id
     */
    int getId();

    /**
     * Returns literals, associated with Record
     */
    Set<Character> literals();
}