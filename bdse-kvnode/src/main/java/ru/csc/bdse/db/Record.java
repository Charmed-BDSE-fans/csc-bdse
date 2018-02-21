package ru.csc.bdse.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;

@Entity
public class Record {
    @Id
    private String key;

    private byte[] value;

    public Record() {
    }

    public Record(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Record{" +
                "key='" + key + '\'' +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}

