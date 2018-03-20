package ru.csc.bdse.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.csc.bdse.app.utils.SerializationUtils;
import ru.csc.bdse.app.v1.phonebook.PhoneBookRecord;

import java.util.Arrays;

import static org.junit.Assert.*;

public class PhoneBookSerializationTest {
    private ObjectMapper mapper = SerializationUtils.getObjectMapperForRecords();

    @Test
    public void testBackwardCompatibility() throws Exception {
        PhoneBookRecord originalRecord = new PhoneBookRecord("Nick", "Fisher", "555-12-44");
        String encoded = mapper.writeValueAsString(originalRecord);
        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord restoredRecord = mapper.readValue(encoded, ru.csc.bdse.app.v2.phonebook.PhoneBookRecord.class);

        assertEquals(originalRecord.getName(), restoredRecord.getName());
        assertEquals(originalRecord.getSurname(), restoredRecord.getSurname());
        assertEquals(originalRecord.getPhone(), restoredRecord.getPhone());

        assertArrayEquals(new String[]{originalRecord.getPhone()}, restoredRecord.getPhones().toArray());
        assertNull(restoredRecord.getNickname());
    }

    @Test
    public void testForwardCompatibility() throws Exception {
        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord originalRecord = new ru.csc.bdse.app.v2.phonebook.PhoneBookRecord(
                "Nick",
                "Fisher",
                "The Kid",
                Arrays.asList("555-12-44", "555-22-33")
        );
        String encoded = mapper.writeValueAsString(originalRecord);
        PhoneBookRecord restoredRecord = mapper.readValue(encoded, PhoneBookRecord.class);

        assertEquals(originalRecord.getName(), restoredRecord.getName());
        assertEquals(originalRecord.getSurname(), restoredRecord.getSurname());
        assertEquals(originalRecord.getPhone(), restoredRecord.getPhone());
    }

    @Test
    public void test3() throws Exception {
        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord originalRecord = new ru.csc.bdse.app.v2.phonebook.PhoneBookRecord(
                "Nick",
                "Fisher",
                "The Kid",
                Arrays.asList("555-12-44", "555-22-33")
        );

        String encoded = mapper.writeValueAsString(originalRecord);

        ru.csc.bdse.app.v2.phonebook.PhoneBookRecord restoredRecord = mapper.readValue(encoded, ru.csc.bdse.app.v2.phonebook.PhoneBookRecord.class);

        assertEquals(originalRecord.getName(), restoredRecord.getName());
        assertEquals(originalRecord.getSurname(), restoredRecord.getSurname());
        assertEquals(originalRecord.getPhone(), restoredRecord.getPhone());
        assertEquals(originalRecord.getNickname(), restoredRecord.getNickname());
        assertEquals(originalRecord.getPhones(), restoredRecord.getPhones());
    }
}
