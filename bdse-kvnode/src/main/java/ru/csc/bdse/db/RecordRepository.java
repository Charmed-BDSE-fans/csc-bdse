package ru.csc.bdse.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

public interface RecordRepository extends CrudRepository<Record, String> {
    Collection<RecordKey> findByKeyStartingWith(String prefix);

    Optional<Record> findByKey(String key);
}
