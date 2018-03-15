package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;
import ru.csc.bdse.kv.config.PostgresKeyValueApiConfig;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.util.Containers;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PostgresKeyValueApiConfig.PROFILE)
@ContextConfiguration(initializers = PostgresKeyValueApiTest.Initializer.class)
public class PostgresKeyValueApiTest extends AbstractKeyValueApiTest {
    @ClassRule
    public static final Containers.PostgresContainer db = Containers.postgresDB().withNetwork(Network.SHARED);

    @Autowired
    private KeyValueApi api;

    @Override
    protected KeyValueApi newKeyValueApi() {
        return api;
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            db.setupSpringContext(context);
        }
    }
}
