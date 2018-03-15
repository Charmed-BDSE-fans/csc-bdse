package ru.csc.bdse.app.v1.phonebook;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import ru.csc.bdse.app.common.PhoneBookApiClientBase;
import ru.csc.bdse.app.common.Record;
import ru.csc.bdse.kv.KeyValueApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.*;

public class PhoneBookApiClient extends PhoneBookApiClientBase<PhoneBookRecord> {
    public PhoneBookApiClient(KeyValueApi kva) {
        super(kva);
    }

    @Override
    public void put(PhoneBookRecord record) {
        String keyData = "/data/" + Integer.toString(record.getId());
        String keyLetter = "/letter/" + record.getSurname().charAt(0);
        byte[] value;
        try {
            value = mapper.writeValueAsBytes(record);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to put due to a bad JSON");
            return;
        }
        keyValueApi.put(keyData, value);
        keyValueApi.put(keyLetter, keyData.getBytes());
    }

    @Override
    public void delete(PhoneBookRecord record) {
        String key = Integer.toString(record.getId());
        keyValueApi.delete(key);
    }

    @Override
    public Set<PhoneBookRecord> get(char literal) {
        return keyValueApi.getKeys(Character.toString(literal)).stream().map((s) -> {
            try {
                // no check before get() since we know it's from
                // a valid collection
                return mapper.readValue(keyValueApi.get(s).get(), PhoneBookRecord.class);
            } catch (JsonParseException e) {
                LOGGER.warn("Bad json syntax in the record with key ", s);
                return null;
            } catch (JsonMappingException e) {
                LOGGER.warn("Bad json formatting in the record with key ", s);
                return null;
            } catch (IOException e) {
                LOGGER.warn("Failed to decode the record with key ", s);
                return null;
            }
        }).filter(Objects::nonNull).reduce(Collectors.toSet());
    }
}
