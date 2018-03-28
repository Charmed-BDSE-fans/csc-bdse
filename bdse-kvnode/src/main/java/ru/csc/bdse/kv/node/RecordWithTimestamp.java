package ru.csc.bdse.kv.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.csc.bdse.util.Constants;

import java.util.Arrays;
import java.util.Optional;

public final class RecordWithTimestamp {
    private final boolean deleted;
    private final byte[] data;
    private final long timestamp;

    private static final RecordWithTimestamp FAKE = ofDeleted(0);

    protected RecordWithTimestamp() {
        data = Constants.EMPTY_BYTE_ARRAY;
        deleted = false;
        timestamp = 0;
    }

    private RecordWithTimestamp(boolean deleted, byte[] data, long timestamp) {
        this.deleted = deleted;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static RecordWithTimestamp ofDeleted(long timestamp) {
        return new RecordWithTimestamp(true, Constants.EMPTY_BYTE_ARRAY, timestamp);
    }

    public static RecordWithTimestamp ofPresent(byte[] data, long timestamp) {
        return new RecordWithTimestamp(false, data, timestamp);
    }

    public static RecordWithTimestamp fake() {
        return FAKE;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public byte[] getData() {
        return data;
    }

    @JsonIgnore
    public Optional<byte[]> getOptionalData() {
        if (deleted)
            return Optional.empty();
        return Optional.of(data);
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RecordWithTimestamp{" +
                "deleted=" + deleted +
                ", data=" + Arrays.toString(data) +
                ", timestamp=" + timestamp +
                '}';
    }
}
