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
        List<Optional<RecordWithTimestamp>> data = Arrays.asList(Optional.empty(), Optional.empty(), Optional.empty());
        RecordWithTimestamp rec = cf.resolve(data).get();
        assertTrue(rec.isDeleted());
    }

    @Test
    public void TesEquals() {
        List<Optional<RecordWithTimestamp>> data = Arrays.asList(
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0)),
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0)),
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0)),
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0))
        );

        RecordWithTimestamp rec = cf.resolve(data).get();

        assertEquals(0, rec.getTimestamp());
        assertArrayEquals("kek".getBytes(), rec.getData());
    }

    @Test
    public void TestInconsistency() {
        List<Optional<RecordWithTimestamp>> data = Arrays.asList(
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0)),
                Optional.of(RecordWithTimestamp.ofPresent("lol".getBytes(), 1)),
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0)),
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0))
        );

        RecordWithTimestamp rec = cf.resolve(data).get();

        assertEquals(1, rec.getTimestamp());
        assertArrayEquals("lol".getBytes(), rec.getData());
    }

    @Test
    public void TestInconsistency2() {
        List<Optional<RecordWithTimestamp>> data = Arrays.asList(
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0)),
                Optional.of(RecordWithTimestamp.ofPresent("lol".getBytes(), 1)),
                Optional.of(RecordWithTimestamp.ofDeleted(2)),
                Optional.of(RecordWithTimestamp.ofPresent("kek".getBytes(), 0))
        );

        RecordWithTimestamp rec = cf.resolve(data).get();

        assertTrue(rec.isDeleted());
    }
}
