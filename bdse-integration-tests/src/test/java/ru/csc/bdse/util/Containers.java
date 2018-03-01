package ru.csc.bdse.util;

import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.config.InMemoryKeyValueApiConfig;
import ru.csc.bdse.config.PostgresKeyValueApiConfig;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Container utils for testing
 */
public class Containers {
    private static final Duration DEFAULT_TIMEOUT = Duration.of(30, SECONDS);

    /**
     * Creates Postgres DB container.
     */
    public static <T extends PostgresContainer<T>> PostgresContainer<T> postgresDB() {
        return new PostgresContainer<>();
    }

    /**
     * Creates in-memory key-value node with given name.
     * @param name Node name.
     */
    public static <T extends KVNodeContainer<T>> KVNodeContainer<T> inMemoryNode(String name) {
        return new KVNodeContainer<>(InMemoryKeyValueApiConfig.PROFILE, name);
    }

    /**
     * Creates in-memory key-value node with default name.
     */
    public static <T extends KVNodeContainer<T>> KVNodeContainer<T> inMemoryNode() {
        return inMemoryNode(KVNodeContainer.DEFAULT_NODE_NAME);
    }

    /**
     * Creates Postgres-based key-value node with given name.
     * @param name Node name.
     * @param connectionUrl Database connection URL.
     */
    public static <T extends KVNodeContainer<T>> KVNodeContainer<T> postgresNode(String name, String connectionUrl) {
        return new KVNodeContainer<T>(PostgresKeyValueApiConfig.PROFILE, name)
                .withEnv(Env.SPRING_DATASOURCE_URL, connectionUrl);
    }

    /**
     * Creates Postgres-based key-value node with default name.
     * @param connectionUrl Database connection URL.
     */
    public static <T extends KVNodeContainer<T>> KVNodeContainer<T> postgresNode(String connectionUrl) {
        return postgresNode(KVNodeContainer.DEFAULT_NODE_NAME, connectionUrl);
    }

    public static final class PostgresContainer<SELF extends PostgresContainer<SELF>> extends GenericContainer<SELF> {
        private static final String NETWORK_ALIAS = "postgres";
        private static final int POSTGRES_PORT = 5432;

        private PostgresContainer() {
            super("postgres:10-alpine");
            withExposedPorts(POSTGRES_PORT);
            withStartupTimeout(DEFAULT_TIMEOUT);
            withNetworkAliases(NETWORK_ALIAS);
        }

        /**
         * Returns database connection URL.
         *
         * If asked for predefined address, will return address that is known before the container is started,
         * but custom network setup is required (ie, `Network.SHARED` cannot be used).
         *
         * Else, will give runtime address.
         *
         * @param getPredefinedAddress Set true to get predefined address.
         */
        public String getConnectionUrl(boolean getPredefinedAddress) {
            if (getNetwork() == Network.SHARED && getPredefinedAddress)
                throw new IllegalStateException("Cannot get predefined address in shared network setup.");
            String host = NETWORK_ALIAS;
            int port = POSTGRES_PORT;
            if (!getPredefinedAddress) {
                host = getContainerIpAddress();
                port = getMappedPort(POSTGRES_PORT);
            }
            return String.format("jdbc:postgresql://%s:%d/postgres", host, port);
        }

        /**
         * Setups Spring Context to connect to database specified by this container.
         * If you want to run local test with database, call this method from initializer.
         * @see org.springframework.context.ApplicationContextInitializer
         * @param context Context from ApplicationContextInitializer
         */
        public void setupSpringContext(ConfigurableApplicationContext context) {
            EnvironmentTestUtils.addEnvironment(context,
                    String.format("%s=%s", Env.SPRING_DATASOURCE_URL_PROPERTY, getConnectionUrl(false)));
        }
    }

    public static final class KVNodeContainer<SELF extends KVNodeContainer<SELF>> extends GenericContainer<SELF> {
        private static final String DEFAULT_NODE_NAME = "kv-node-0";
        private static final int APPLICATION_PORT = 8080;

        private final String name;

        private KVNodeContainer(String profile, String name) {
            super("charmed-bdse-fans/bdse-kvnode:latest");
            withExposedPorts(APPLICATION_PORT);
            withEnv(Env.KVNODE_NAME, name);
            withEnv(Env.SPRING_PROFILES_ACTIVE, profile);
            withStartupTimeout(DEFAULT_TIMEOUT);
            withNetworkAliases(name);

            this.name = name;
        }

        public String getName() {
            return name;
        }

        /**
         * Returns REST base URL.
         *
         * If asked for predefined address, will return address that is known before the container is started,
         * but custom network setup is required (ie, `Network.SHARED` cannot be used).
         *
         * Else, will give runtime address.
         *
         * @param getPredefinedAddress Set true to get predefined address.
         */
        public String getRESTBaseUrl(boolean getPredefinedAddress) {
            if (getNetwork() == Network.SHARED && getPredefinedAddress)
                throw new IllegalStateException("Cannot get predefined address in shared network setup.");
            String host = name;
            int port = APPLICATION_PORT;
            if (!getPredefinedAddress) {
                host = getContainerIpAddress();
                port = getMappedPort(APPLICATION_PORT);
            }
            return String.format("http://%s:%d", host, port);
        }
    }

    private Containers() { }
}
