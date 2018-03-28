package ru.csc.bdse.kv.node;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CoordinatedKeyValueApi implements KeyValueApi {
    public CoordinatedKeyValueApi(int rcl, int wcl, int timeout, List<InternalKeyValueApi> apis, ObjectMapper mapper) { }

    @Override
    public void put(String key, byte[] value) { }

    @Override
    public Optional<byte[]> get(String key) {
        return null;
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return null;
    }

    @Override
    public void delete(String key) { }

    @Override
    public Set<NodeInfo> getInfo() {
        return null;
    }

    @Override
    public void action(String node, NodeAction action) { }

    @Override
    public void deleteAll() { }
}
