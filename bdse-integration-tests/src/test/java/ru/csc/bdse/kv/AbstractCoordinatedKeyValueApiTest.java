package ru.csc.bdse.kv;

import org.junit.Assert;
import org.junit.Test;
import ru.csc.bdse.kv.node.CoordinatedKeyValueApi;
import ru.csc.bdse.kv.node.NodeAction;
import ru.csc.bdse.util.Random;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public abstract class AbstractCoordinatedKeyValueApiTest {
    protected abstract CoordinatedKeyValueApi getApi();

    CoordinatedKeyValueApi api = getApi();

    @Test
    public void basicReadWriteTest() {
        final int ITEMS = 100;

        for (int i = 0; i < ITEMS; i++) {
            api.put(Integer.toString(i), Integer.toString(i).getBytes());
        }

        for (int i = 0; i < ITEMS; i++) {
            Assert.assertArrayEquals(Integer.toString(i).getBytes(), api.get(Integer.toString(i)).get());
        }

        final int PREFIX_SIZE = 11;

        assertEquals(PREFIX_SIZE, api.getKeys("2").size());
    }


    @Test
    public void basicWriteDeleteTest() {
        final int ITEMS = 100;

        for (int i = 0; i < ITEMS; i++) {
            api.put(Integer.toString(i), Integer.toString(i).getBytes());
        }

        final int DELETE_BEGIN = 42;
        final int DELETE_END = 56;

        for (int i = DELETE_BEGIN; i < DELETE_END; i++) {
            api.delete(Integer.toString(i));
        }

        for (int i = 0; i < ITEMS; i++) {
            if (DELETE_BEGIN <= i && i < DELETE_END) {
                assertFalse(api.get(Integer.toString(i)).isPresent());
            } else {
                Assert.assertArrayEquals(Integer.toString(i).getBytes(), api.get(Integer.toString(i)).get());
            }
        }
    }

    protected abstract List<String> nodesToBeOff();

    @Test
    public void inconsistencyWriteTest() {
        String key = Random.nextKey();
        byte[] data1 = Random.nextValue();
        byte[] data2 = Random.nextValue();

        api.put(key, data1);

        for (String node : nodesToBeOff()) {
            api.action(node, NodeAction.DOWN);
        }

        api.put(key, data2);

        for (String node : nodesToBeOff()) {
            api.action(node, NodeAction.UP);
        }

        byte[] actualData = api.get(key).get();

        assertArrayEquals(data2, actualData);
    }

    @Test
    public void inconsistencyDeleteTest() {
        String key = Random.nextKey();
        byte[] data = Random.nextValue();

        api.put(key, data);

        for (String node : nodesToBeOff()) {
            api.action(node, NodeAction.DOWN);
        }

        api.delete(key);

        for (String node : nodesToBeOff()) {
            api.action(node, NodeAction.UP);
        }

        Optional<?> actualData = api.get(key);

        assertFalse(actualData.isPresent());
    }
}
