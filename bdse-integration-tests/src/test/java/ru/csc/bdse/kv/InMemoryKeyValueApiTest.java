package ru.csc.bdse.kv;

import ru.csc.bdse.kv.node.InMemoryKeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApi;

/**
 * @author semkagtn
 */
public class InMemoryKeyValueApiTest extends AbstractKeyValueApiTest {

    @Override
    protected KeyValueApi newKeyValueApi() {
        return new InMemoryKeyValueApi("node");
    }
}
