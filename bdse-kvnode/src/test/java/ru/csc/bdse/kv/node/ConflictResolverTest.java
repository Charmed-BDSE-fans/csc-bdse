package ru.csc.bdse.kv.node;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ConflictResolverTest {
    private final ConflictResolver cf = new TimestampConflictResolver();


    @Test
    public void TestAllEmpty() {
        List<RecordWithTimestamp> data = Arrays.asList(
                RecordWithTimestamp.fake(), RecordWithTimestamp.fake(), RecordWithTimestamp.fake());
        RecordWithTimestamp rec = cf.resolve(data).get();
        assertTrue(rec.isDeleted());
    }

    @Test
    public void TesEquals() {
        List<RecordWithTimestamp> data = Arrays.asList(
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0),
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0),
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0),
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0)
        );

        RecordWithTimestamp rec = cf.resolve(data).get();

        assertEquals(0, rec.getTimestamp());
        assertArrayEquals("kek".getBytes(), rec.getData());
    }

    @Test
    public void TestInconsistency() {
        List<RecordWithTimestamp> data = Arrays.asList(
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0),
                RecordWithTimestamp.ofPresent("lol".getBytes(), 1),
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0),
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0)
        );

        RecordWithTimestamp rec = cf.resolve(data).get();

        assertEquals(1, rec.getTimestamp());
        assertArrayEquals("lol".getBytes(), rec.getData());
    }

    @Test
    public void TestInconsistency2() {
        List<RecordWithTimestamp> data = Arrays.asList(
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0),
                RecordWithTimestamp.ofPresent("meow".getBytes(), 1),
                RecordWithTimestamp.ofDeleted(2),
                RecordWithTimestamp.ofPresent("kek".getBytes(), 0)
        );

        RecordWithTimestamp rec = cf.resolve(data).get();

        assertTrue(rec.isDeleted());
    }
}
