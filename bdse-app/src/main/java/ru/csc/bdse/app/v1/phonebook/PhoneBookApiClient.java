package ru.csc.bdse.app.v1.phonebook;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import ru.csc.bdse.app.common.PhoneBookApiClientBase;
import ru.csc.bdse.kv.KeyValueApi;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PhoneBookApiClient extends PhoneBookApiClientBase<PhoneBookRecord> {
    public PhoneBookApiClient(KeyValueApi kva) {
        super(kva);
    }

    @Override
    public void put(PhoneBookRecord record) {
        String keyData = makeDataKey(record.getId());
        String keyLetter = makeLetterKey(record.getSurname().charAt(0));

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
        String keyData = makeDataKey(record.getId());
        String keyLetter = makeLetterKey(record.getSurname().charAt(0));
        keyValueApi.delete(keyData);
        keyValueApi.delete(keyLetter);
    }

    @Override
    public Set<PhoneBookRecord> get(char literal) {
        String keyLetter = makeLetterKey(literal);
        return keyValueApi.getKeys(keyLetter).stream().map((key) -> {
            try {
                // no check before get() since we know it's from
                // a valid collection
                return mapper.readValue(keyValueApi.get(key).get(), PhoneBookRecord.class);
            } catch (JsonParseException e) {
                LOGGER.warn("Bad json syntax in the record with key ", key);
                return null;
            } catch (JsonMappingException e) {
                LOGGER.warn("Bad json formatting in the record with key ", key);
                return null;
            } catch (IOException e) {
                LOGGER.warn("Failed to decode the record with key ", key);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
