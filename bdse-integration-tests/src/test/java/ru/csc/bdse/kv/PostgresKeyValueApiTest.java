package ru.csc.bdse.kv;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.Application;
import ru.csc.bdse.config.PostgresKeyValueApiConfig;
import ru.csc.bdse.util.Containers;

@RunWith(SpringRunner.class)
@ActiveProfiles(PostgresKeyValueApiConfig.PROFILE)
@ContextConfiguration(
        classes = Application.class,
        initializers = PostgresKeyValueApiTest.Initializer.class)
public class PostgresKeyValueApiTest extends AbstractKeyValueApiTest implements ApplicationContextAware {
    @ClassRule
    public static final GenericContainer db = Containers.postgres(Network.SHARED);

    private ApplicationContext context;

    @Override
    protected KeyValueApi newKeyValueApi() {
        Assert.assertNotNull(context);
        KeyValueApi api = context.getBean(KeyValueApi.class);
        Assert.assertNotNull(api);
        return api;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            Containers.setupSpringContextForPostgres(context, db);
        }
    }
}
