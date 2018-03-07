package ru.csc.bdse.db;

import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

public interface RecordRepository extends CrudRepository<Record, String> {
    @Transactional
    default boolean deleteIfExists(String key) {
        if (exists(key)) {
            delete(key);
            return true;
        }
        return false;
    }

    Collection<RecordKey> findByKeyStartingWith(String prefix);

    Optional<Record> findByKey(String key);
}
