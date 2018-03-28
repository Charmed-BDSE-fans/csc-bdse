package ru.csc.bdse.kv.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class RecordWithTimestampSerializationTest {
    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    public void testSerializationWithData() throws Exception {
        RecordWithTimestamp original = new RecordWithTimestamp(false, new byte[]{1, 2, 3}, 12345);
        String encoded = mapper.writeValueAsString(original);
        RecordWithTimestamp actual = mapper.readValue(encoded, RecordWithTimestamp.class);

        assertTrue(actual.getOptionalData().isPresent());
        assertArrayEquals(original.getData(), actual.getData());
        assertFalse(actual.isDeleted());
        assertEquals(original.getTimestamp(), actual.getTimestamp());
    }

    @Test
    public void testSerializationWithoutData() throws Exception {
        RecordWithTimestamp original = new RecordWithTimestamp(true, new byte[]{1, 2, 3}, 12345);
        String encoded = mapper.writeValueAsString(original);
        RecordWithTimestamp actual = mapper.readValue(encoded, RecordWithTimestamp.class);

        assertFalse(actual.getOptionalData().isPresent());
        assertTrue(actual.isDeleted());
        assertEquals(original.getTimestamp(), actual.getTimestamp());
    }
}