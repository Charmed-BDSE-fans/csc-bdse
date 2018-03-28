package ru.csc.bdse.kv.node;

import ru.csc.bdse.util.Constants;

import java.util.Arrays;

public class RecordWithTimestamp {
    private final boolean deleted;
    private final byte[] data;

    protected RecordWithTimestamp(boolean deleted, byte[] data) {
        this.deleted = deleted;
        this.data = data;
    }

    public static RecordWithTimestamp ofDeleted() {
        return new RecordWithTimestamp(true, Constants.EMPTY_BYTE_ARRAY);
    }

    public static RecordWithTimestamp ofPresent(byte[] data) {
        return new RecordWithTimestamp(false, data);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "RecordWithTimestamp{" +
                "deleted=" + deleted +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
