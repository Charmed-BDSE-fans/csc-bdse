package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.testcontainers.containers.Network;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApiHttpClient;
import ru.csc.bdse.util.Containers;

/**
 * @author semkagtn
 */
public class KeyValueApiHttpClientTest extends AbstractKeyValueApiTest {
    @ClassRule
    public static final Containers.KVNodeContainer node = Containers
            .inMemoryNode()
            .withNetwork(Network.SHARED);

    @Override
    protected KeyValueApi newKeyValueApi() {
        return new KeyValueApiHttpClient(node.getRESTBaseUrl(false));
    }
}
