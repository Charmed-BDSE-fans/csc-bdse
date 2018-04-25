package ru.csc.bdse.kv;

import ru.csc.bdse.kv.node.InMemoryKeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.node.PartitioningKeyValueApi;
import ru.csc.bdse.partitioning.FirstLetterPartitioner;
import ru.csc.bdse.partitioning.Partitioner;

import java.util.*;

public class PartitionedSingleFailureFirstLetterTest extends AbstractPartitionedKeyValueApiHttpClientTest {

    private final String name0 = "0";
    private final String name1 = "1";
    private final String name2 = "2";
    private final InMemoryKeyValueApi node0 = new InMemoryKeyValueApi(name0);
    private final InMemoryKeyValueApi node1 = new InMemoryKeyValueApi(name1);
    private final InMemoryKeyValueApi node2 = new InMemoryKeyValueApi(name2);

    @Override
    protected KeyValueApi newCluster1() {
        Set<String> partitions = new HashSet<>(Arrays.asList(name0, name1, name2));
        Partitioner partitioner = new FirstLetterPartitioner(partitions);
        Map<String, KeyValueApi> nodes = new HashMap<>();
        nodes.put(name0, node0);
        nodes.put(name1, node1);
        nodes.put(name2, node2);
        return new PartitioningKeyValueApi(nodes, 3, partitioner);
    }

    @Override
    protected KeyValueApi newCluster2() {
        Set<String> partitions = new HashSet<>(Arrays.asList(name0, name2));
        Partitioner partitioner = new FirstLetterPartitioner(partitions);
        Map<String, KeyValueApi> nodes = new HashMap<>();
        nodes.put(name0, node0);
        nodes.put(name2, node2);
        return new PartitioningKeyValueApi(nodes, 3, partitioner);
    }

    @Override
    protected float expectedKeysLossProportion() {
        return ((float)(node0.getKeys("").size() + node1.getKeys("").size() + node2.getKeys("").size())) / 3;
    }

    @Override
    protected float expectedUndeletedKeysProportion() {
        return ((float)(node0.getKeys("").size() + node1.getKeys("").size() + node2.getKeys("").size())) / 3;
    }
}
