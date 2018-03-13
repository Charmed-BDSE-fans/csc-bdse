package ru.csc.bdse.app;

import org.junit.Before;
import org.junit.Test;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.common.Record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public abstract class AbstractPhoneBookFunctionalTest {

    protected abstract Record randomRecord();

    protected abstract Record modifyContent(Record r);

    protected abstract PhoneBookApi client();

    private PhoneBookApi api = client();

    @Before
    public void cleanPB() {
        // api.deleteAll();
    }

    @Test
    public void getFromEmptyBook() {
        // get records from an empty phone book
        Set<? extends Record> s = api.get('c');

        assertTrue(s.isEmpty());
    }

    @Test
    public void putAndGet() {
        // write some data and read it

        final int RECORDS_NUM = 100;

        final Set<Record> added = new HashSet<>();

        for (int i = 0; i < RECORDS_NUM; i++) {
            Record r = randomRecord();
            api.put(r);
            added.add(r);
        }


        final Set<Record> found = new HashSet<>();
        found.addAll(added);

        for (Record r : added) {
            char c = new ArrayList<>(r.literals()).get(0);
            Set<Record> s = api.get(c);
            found.removeAll(s);
        }

        assertTrue(found.isEmpty());
    }

    @Test
    public void erasure() {
        // cancel some records

        final int RECORDS_NUM = 100;

        final Set<Record> added = new HashSet<>();

        for (int i = 0; i < RECORDS_NUM; i++) {
            Record r = randomRecord();
            api.put(r);
            added.add(r);
        }

        for (Record r : added) {
            api.delete(r);
        }

        for (Record r : added) {
            char c = new ArrayList<>(r.literals()).get(0);
            Set<Record> s = api.get(c);
            assertTrue(s.isEmpty());
        }
    }

    @Test
    public void update() {
        // update data and put some data twice

        final int RECORDS_NUM = 100;

        final Record record = randomRecord();

        api.put(record);

        for (int i = 0; i < RECORDS_NUM; i++) {
            final Record r = randomRecord();
            api.put(r);
        }

        final Record modifiedRecord = modifyContent(record);

        api.put(modifiedRecord);

        char c = new ArrayList<>(modifiedRecord.literals()).get(0);

        Set<Record> found = api.get(c);

        assertTrue(found.contains(modifiedRecord));
    }
}