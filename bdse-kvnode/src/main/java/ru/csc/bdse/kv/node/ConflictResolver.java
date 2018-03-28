package ru.csc.bdse.kv.node;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConflictResolver {

    Optional<byte[]> resolve(List<Optional<byte[]>> values);
    Set<String> resolveKeys(Set<Set<String>> keys);
}
