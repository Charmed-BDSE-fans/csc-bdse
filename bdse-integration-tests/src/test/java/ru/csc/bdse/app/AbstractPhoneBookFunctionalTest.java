package ru.csc.bdse.app;

import org.junit.Before;
import org.junit.Test;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.common.Record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public abstract class AbstractPhoneBookFunctionalTest<R extends Record> {
    protected abstract R randomRecord();

    protected abstract R modifyContent(R record);

    protected abstract String getSurname(R record);

    protected abstract R modifySurname(R record);

    protected abstract boolean equalContent(R record1, R record2);

    protected abstract PhoneBookApi<R> client();

    private PhoneBookApi<R> api = null;

    private synchronized PhoneBookApi<R> getPhoneBookApi() {
        if (api == null) {
            api = client();
        }
        return api;
    }

    @Before
    public void cleanPB() {
        getPhoneBookApi().deleteAll();
    }

    @Test
    public void getFromEmptyBook() {
        // get records from an empty phone book
        PhoneBookApi<R> api = getPhoneBookApi();
        Set<R> s = api.get('c');

        assertTrue(s.isEmpty());
    }

    @Test
    public void putAndGet() {
        // write some data and read it
        PhoneBookApi<R> api = getPhoneBookApi();

        final int RECORDS_NUM = 100;

        Set<R> added = new HashSet<>();

        for (int i = 0; i < RECORDS_NUM; i++) {
            R r = randomRecord();
            api.put(r);
            added.add(r);
        }

        Set<R> found = new HashSet<>(added);

        for (R r : added) {
            char c = new ArrayList<>(r.literals()).get(0);
            Set<R> s = api.get(c);
            found.removeAll(s);
        }

        assertTrue(found.isEmpty());
    }

    @Test
    public void erasure() {
        // cancel some records
        PhoneBookApi<R> api = getPhoneBookApi();

        final int RECORDS_NUM = 100;

        final Set<R> added = new HashSet<>();

        for (int i = 0; i < RECORDS_NUM; i++) {
            R r = randomRecord();
            api.put(r);
            added.add(r);
        }

        for (R r : added) {
            api.delete(r);
        }

        for (R r : added) {
            char c = new ArrayList<>(r.literals()).get(0);
            Set<R> s = api.get(c);
            assertTrue(s.isEmpty());
        }
    }

    @Test
    public void update() {
        // update data and put some data twice
        PhoneBookApi<R> api = getPhoneBookApi();

        final int RECORDS_NUM = 100;

        final R record = randomRecord();

        api.put(record);

        for (int i = 0; i < RECORDS_NUM; i++) {
            final R r = randomRecord();
            api.put(r);
        }

        final R modifiedRecord = modifyContent(record);

        api.put(modifiedRecord);

        char c = new ArrayList<>(modifiedRecord.literals()).get(0);

        Set<R> found = api.get(c);

        assertTrue(found.contains(modifiedRecord));

        R readModifiedRecord = found.stream()
                .filter(o -> o.equals(modifiedRecord))
                .collect(Collectors.toList())
                .get(0);

        assertFalse(equalContent(record, readModifiedRecord));
    }

    @Test
    public void updateThanGet() {
        final R oldRecord = randomRecord();
        final R newRecord = modifySurname(oldRecord); // modifies surname

        final char oldSurnameFirstLetter = getSurname(oldRecord).charAt(0);
        final char newSurnameFirstLetter = getSurname(newRecord).charAt(0);
        assumeTrue(oldSurnameFirstLetter != newSurnameFirstLetter);

        api.put(oldRecord);
        api.put(newRecord);

        int actualOldSize = api.get(oldSurnameFirstLetter).size();
        int expectedOldSize = 1;

        assertEquals("Actual size: " + actualOldSize + " but size: " + expectedOldSize, expectedOldSize, actualOldSize);

        int actualNewSize = api.get(oldSurnameFirstLetter).size();
        int expectedNewSize = 1;

        assertEquals("Actual size: " + actualNewSize + " but size: " + expectedNewSize, expectedNewSize, actualNewSize);
    }
}