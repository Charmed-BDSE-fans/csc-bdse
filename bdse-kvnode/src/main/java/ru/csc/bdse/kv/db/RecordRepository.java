package ru.csc.bdse.kv.db;

import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

public interface RecordRepository extends CrudRepository<Record, String> {
    @Transactional
    default boolean deleteIfExists(String key) {
        if (existsById(key)) {
            deleteById(key);
            return true;
        }
        return false;
    }

    Collection<RecordKey> findByKeyStartingWith(String prefix);

    Optional<Record> findByKey(String key);
}
