package ru.csc.bdse.kv.node;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConflictResolver {

    Optional<RecordWithTimestamp> resolve(List<RecordWithTimestamp> values);
    Set<String> resolveKeys(List<Set<String>> keys);
}
