package ru.csc.bdse.app.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.csc.bdse.app.utils.SerializationUtils;
import ru.csc.bdse.kv.node.KeyValueApi;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class KeyValuePhoneBookApiBase<R extends Record> implements PhoneBookApi<R> {
    private final static String LETTER_PREFIX = "/letter/";
    private final static String DATA_PREFIX = "/data/";

    private final KeyValueApi keyValueApi;
    private final ObjectMapper mapper = SerializationUtils.getObjectMapperForRecords();
    private final Class<R> recordClass;

    private static final byte[] EMPTY_ARRAY = new byte[0];

    private static <R extends Record> Set<String> makeLetterKeys(R record) {
        return record
                .literals()
                .stream()
                .map(c -> String.format("%s%c|%d", LETTER_PREFIX, c, record.getId()))
                .collect(Collectors.toSet());
    }

    private static String makeLetterKeyPrefix(char c) {
        return String.format("%s%c", LETTER_PREFIX, c);
    }

    private static int getIdFromLetterKey(String key) {
        return Integer.parseInt(key.replaceAll("^[^|]+|", ""));
    }

    private static String makeDataKey(int id) {
        return String.format("%s%d", DATA_PREFIX, id);
    }

    public KeyValuePhoneBookApiBase(KeyValueApi keyValueApi, Class<R> recordClass) {
        this.keyValueApi = keyValueApi;
        this.recordClass = recordClass;
    }

    @Override
    public void put(R record) {
        byte[] data;
        try {
            data = mapper.writeValueAsBytes(record);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Serialization failed", e);
        }
        String dataKey = makeDataKey(record.getId());
        Set<String> letterKeys = makeLetterKeys(record);

        keyValueApi.put(dataKey, data);
        for (String letterKey: letterKeys) {
            keyValueApi.put(letterKey, EMPTY_ARRAY);
        }
    }

    @Override
    public Set<R> get(char literal) {
        Set<String> keys = keyValueApi.getKeys(makeLetterKeyPrefix(literal));
        return keys.stream()
                .map(k -> keyValueApi.get(makeDataKey(getIdFromLetterKey(k))))
                // Really need Optional.stream() from Java 9 here...
                .flatMap(t -> t.map(Stream::of).orElse(Stream.empty()))
                .map(b -> {
                    try {
                        return mapper.readValue(b, recordClass);
                    } catch (IOException e) {
                        throw new IllegalStateException("Corrupted data", e);
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public void delete(R record) {
        String dataKey = makeDataKey(record.getId());
        Set<String> letterKeys = makeLetterKeys(record);

        keyValueApi.delete(dataKey);
        for (String letterKey : letterKeys) {
            keyValueApi.delete(letterKey);
        }
    }
}
