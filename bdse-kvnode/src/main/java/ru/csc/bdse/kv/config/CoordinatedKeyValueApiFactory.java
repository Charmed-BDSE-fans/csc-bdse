package ru.csc.bdse.kv.config;

import ru.csc.bdse.kv.node.KeyValueApi;

public interface CoordinatedKeyValueApiFactory {
    KeyValueApi coordinateWithLocal(KeyValueApi localApi);
}
