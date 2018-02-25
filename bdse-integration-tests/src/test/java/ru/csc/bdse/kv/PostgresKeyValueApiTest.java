package ru.csc.bdse.kv;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.config.PostgresKeyValueApiConfig;
import ru.csc.bdse.util.Containers;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PostgresKeyValueApiConfig.PROFILE)
@ContextConfiguration(initializers = PostgresKeyValueApiTest.Initializer.class)
public class PostgresKeyValueApiTest extends AbstractKeyValueApiTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresKeyValueApiTest.class);

    @ClassRule
    public static final GenericContainer db = Containers.postgres(Network.SHARED);

    @Autowired
    private KeyValueApi api;

    @Override
    protected KeyValueApi newKeyValueApi() {
        return api;
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            Containers.setupSpringContextForPostgres(context, db);
        }
    }
}
