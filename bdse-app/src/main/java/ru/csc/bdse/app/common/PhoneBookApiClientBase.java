package ru.csc.bdse.app.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.csc.bdse.app.utils.SerializationUtils;
import ru.csc.bdse.kv.KeyValueApi;

import java.util.Random;

public abstract class PhoneBookApiClientBase<R extends Record> implements PhoneBookApi<R> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(PhoneBookApiClientBase.class);

    protected final KeyValueApi keyValueApi;
    protected final ObjectMapper mapper = SerializationUtils.getObjectMapperForRecords();

    private final Random rand;

    protected int getId() {
        return rand.nextInt();
    }

    public PhoneBookApiClientBase(KeyValueApi kva) {
        keyValueApi = kva;
        rand = new Random();
    }
}
