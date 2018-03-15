package ru.csc.bdse.app.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.csc.bdse.app.utils.SerializationUtils;
import ru.csc.bdse.kv.KeyValueApi;

import java.util.Random;

public abstract class PhoneBookApiClientBase<R extends Record> implements PhoneBookApi<R> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(PhoneBookApiClientBase.class);
    protected final static String LETTER_PREFIX = "/letter/";
    protected final static String DATA_PREFIX = "/data/";

    protected final KeyValueApi keyValueApi;
    protected final ObjectMapper mapper = SerializationUtils.getObjectMapperForRecords();

    private final Random rand;

    protected int getId() {
        return rand.nextInt();
    }

    protected String makeLetterKey(Character literal) {
        return LETTER_PREFIX + literal;
    }

    protected String makeDataKey(int data) {
        return DATA_PREFIX + Integer.toString(data);
    }

    public PhoneBookApiClientBase(KeyValueApi kva) {
        keyValueApi = kva;
        rand = new Random();
    }
}
