package ru.csc.bdse.kv.node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimestampConflictResolver implements ConflictResolver {
    private static class ByteArray {
        private final byte[] data;

        private ByteArray(byte[] data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ByteArray byteArray = (ByteArray) o;
            return Arrays.equals(data, byteArray.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }
    }

    @Override
    public Optional<RecordWithTimestamp> resolve(List<Optional<RecordWithTimestamp>> maybeValues) {
        if (maybeValues.size() == 0) {
            throw new IllegalArgumentException("Empty argument");
        }
        List<RecordWithTimestamp> values = maybeValues.stream()
                .map(v -> v.orElse(RecordWithTimestamp.fake()))
                .collect(Collectors.toList());

        long maxTimestamp = values.stream()
                .mapToLong(RecordWithTimestamp::getTimestamp)
                .max()
                .getAsLong();
        List<Integer> withMaxTimestamp = IntStream.range(0, values.size())
                .filter(i -> values.get(i).getTimestamp() == maxTimestamp)
                .boxed()
                .collect(Collectors.toList());

        Map<ByteArray, Long> counts = withMaxTimestamp.stream()
                .collect(Collectors.groupingBy(i -> new ByteArray(values.get(i).getData()), Collectors.counting()));
        long maxCount = counts.values()
                .stream()
                .max(Comparator.naturalOrder())
                .get();

        Set<ByteArray> popular = counts.entrySet().stream()
                .filter(e -> e.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return withMaxTimestamp.stream()
                .filter(i -> popular.contains(new ByteArray(values.get(i).getData())))
                .max(Comparator.naturalOrder())
                .map(values::get);
    }

    @Override
    public Set<String> resolveKeys(List<Set<String>> keys) {
        return keys.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
