package ru.csc.bdse.kv;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.RemoteDockerImage;
import ru.csc.bdse.config.PostgresKeyValueApiConfig;
import ru.csc.bdse.util.Containers;

@SpringBootTest(classes = PostgresKeyValueApiConfig.class)
@ActiveProfiles("postgres")
@RunWith(SpringJUnit4ClassRunner.class)
public class PostgresKeyValueApiTest extends AbstractKeyValueApiTest {
    @ClassRule
    public static final GenericContainer db = Containers.postgres(Network.SHARED);

    @Autowired
    private KeyValueApi api;

    @Override
    protected KeyValueApi newKeyValueApi() {
        return api;
    }
}
