package ru.csc.bdse.kv;

import ru.csc.bdse.db.Record;
import ru.csc.bdse.db.RecordKey;
import ru.csc.bdse.db.RecordRepository;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PostgresKeyValueApi implements KeyValueApi {
    private final String name;
    private final RecordRepository repository;

    public PostgresKeyValueApi(String name, RecordRepository repository) {
        this.name = name;
        this.repository = repository;
    }

    @Override
    public void put(String key, byte[] value) {
        repository.save(new Record(key, value));
    }

    @Override
    public Optional<byte[]> get(String key) {
        return repository
                .findByKey(key)
                .map(Record::getValue);
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return repository
                .findByKeyStartingWith(prefix)
                .stream()
                .map(RecordKey::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public void delete(String key) {
        repository.deleteIfExists(key);
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return Collections.singleton(new NodeInfo(name, NodeStatus.UP));
    }

    @Override
    public void action(String node, NodeAction action) {
        throw new RuntimeException("action not implemented now");
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
