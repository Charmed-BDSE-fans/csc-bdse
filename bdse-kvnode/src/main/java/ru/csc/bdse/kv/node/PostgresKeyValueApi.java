package ru.csc.bdse.kv.node;

import ru.csc.bdse.kv.db.Record;
import ru.csc.bdse.kv.db.RecordKey;
import ru.csc.bdse.kv.db.RecordRepository;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PostgresKeyValueApi implements InternalKeyValueApi {
    private final String name;
    private final RecordRepository repository;
    private volatile NodeStatus status;

    public PostgresKeyValueApi(String name, RecordRepository repository) {
        this.name = name;
        this.repository = repository;
        status = NodeStatus.UP;
    }

    @Override
    public void put(String key, byte[] value) {
        if (status == NodeStatus.DOWN) {
            return;
        }

        repository.save(new Record(key, value));
    }

    @Override
    public Optional<byte[]> get(String key) {
        if (status == NodeStatus.DOWN) {
            return Optional.empty();
        }

        return repository
                .findByKey(key)
                .map(Record::getValue);
    }

    @Override
    public Set<String> getKeys(String prefix) {
        if (status == NodeStatus.DOWN) {
            return Collections.emptySet();
        }

        return repository
                .findByKeyStartingWith(prefix)
                .stream()
                .map(RecordKey::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public void delete(String key) {
        if (status == NodeStatus.DOWN) {
            return;
        }

        repository.deleteIfExists(key);
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return Collections.singleton(new NodeInfo(name, status));
    }

    @Override
    public void action(String node, NodeAction action) {
        if (!Objects.equals(name, node)) {
            return;
        }

        switch (action) {
            case UP:
                status = NodeStatus.UP;
                break;
            case DOWN:
                status = NodeStatus.DOWN;
                break;
        }
    }

    @Override
    public void deleteAll() {
        if (status == NodeStatus.DOWN) {
            return;
        }

        repository.deleteAll();
    }
}
