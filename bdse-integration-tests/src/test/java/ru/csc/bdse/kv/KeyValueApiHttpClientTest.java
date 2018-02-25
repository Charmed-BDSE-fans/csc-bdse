package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import ru.csc.bdse.config.InMemoryKeyValueApiConfig;
import ru.csc.bdse.util.Containers;
import ru.csc.bdse.util.Env;

import java.io.File;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author semkagtn
 */
public class KeyValueApiHttpClientTest extends AbstractKeyValueApiTest {

    private static final String NODE_NAME = "node-0";

    @ClassRule
    public static final GenericContainer node = Containers.kvnode(Network.SHARED, NODE_NAME, InMemoryKeyValueApiConfig.PROFILE);

    @Override
    protected KeyValueApi newKeyValueApi() {
        return new KeyValueApiHttpClient(Containers.getKVNodeBaseUrl(node));
    }
}
