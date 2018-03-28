package ru.csc.bdse.kv.db;

public class RecordWithTimestamp {
    private final boolean isDeleted;

    public RecordWithTimestamp(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public String toString() {
        return "RecordWithTimestamp{" +
                "isDeleted=" + isDeleted +
                '}';
    }
}
