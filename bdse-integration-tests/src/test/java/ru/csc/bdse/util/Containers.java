package ru.csc.bdse.util;

import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.config.PhoneBookApiV1Config;
import ru.csc.bdse.app.config.PhoneBookApiV2Config;
import ru.csc.bdse.kv.config.InMemoryKeyValueApiConfig;
import ru.csc.bdse.kv.config.KeyValueApiHttpClientConfig;
import ru.csc.bdse.kv.config.PostgresKeyValueApiConfig;
import ru.csc.bdse.kv.util.Env;

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
    public static PostgresContainer postgresDB() {
        return new PostgresContainer();
    }

    /**
     * Creates in-memory key-value node with given name.
     * @param name Node name.
     */
    public static KVNodeContainer inMemoryNode(String name) {
        return new KVNodeContainer(InMemoryKeyValueApiConfig.PROFILE, name);
    }

    /**
     * Creates in-memory key-value node with default name.
     */
    public static KVNodeContainer inMemoryNode() {
        return inMemoryNode(KVNodeContainer.DEFAULT_NODE_NAME);
    }

    /**
     * Creates Postgres-based key-value node with given name.
     * @param name Node name.
     * @param connectionUrl Database connection URL.
     */
    public static KVNodeContainer postgresNode(String name, String connectionUrl) {
        return new KVNodeContainer(PostgresKeyValueApiConfig.PROFILE, name)
                .withEnv(Env.SPRING_DATASOURCE_URL, connectionUrl);
    }

    private static AppContainer application(AppContainer.Version version, String kvNodeProfile) {
        String appProfile;
        switch (version) {
            case V1:
                appProfile = PhoneBookApiV1Config.PROFILE;
                break;
            case V2:
                appProfile = PhoneBookApiV2Config.PROFILE;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return new AppContainer()
                .withEnv(Env.SPRING_PROFILES_ACTIVE, String.join(",", appProfile, kvNodeProfile));
    }

    /**
     * Creates app container which runs on internal in-memory kvNode.
     * @param version PhoneBookApi version
     */
    public static AppContainer applicationWithInMemoryKV(AppContainer.Version version) {
        return application(version, InMemoryKeyValueApiConfig.PROFILE);
    }

    /**
     * Creates app container which runs on internal postgres kvNode
     * @param version PhoneBookApi version
     * @param dbUrl Postgres connection Url
     */
    public static AppContainer applicationWithPostgresKV(AppContainer.Version version, String dbUrl) {
        return application(version, PostgresKeyValueApiConfig.PROFILE)
                .withEnv(Env.SPRING_DATASOURCE_URL, dbUrl);
    }

    /**
     * Creates app container which runs on remote kvNode
     * @param version PhoneBookApi version
     * @param baseUrl KvNode connection Url
     */
    public static AppContainer applicationWithRemoteKV(AppContainer.Version version, String baseUrl) {
        return application(version, KeyValueApiHttpClientConfig.PROFILE)
                .withEnv(Env.KVNODE_BASEURL, baseUrl);
    }

    /**
     * Creates Postgres-based key-value node with default name.
     * @param connectionUrl Database connection URL.
     */
    public static KVNodeContainer postgresNode(String connectionUrl) {
        return postgresNode(KVNodeContainer.DEFAULT_NODE_NAME, connectionUrl);
    }

    private static abstract class LocatableContainer<T extends LocatableContainer<T>> extends GenericContainer<T> {
        private final String defaultNetworkAlias;
        private final int applicationPort;

        protected LocatableContainer(String image, String defaultNetworkAlias, int applicationPort) {
            super(image);
            withNetworkAliases(defaultNetworkAlias);
            withExposedPorts(applicationPort);
            this.defaultNetworkAlias = defaultNetworkAlias;
            this.applicationPort = applicationPort;
        }

        /**
         * Returns host:port part of connection URL.
         *
         * If asked for predefined address, will return address that is known before the container is started,
         * but custom network setup is required (ie, `Network.SHARED` cannot be used).
         *
         * Else, will give runtime address.
         *
         * @param getPredefinedAddress Set true to get predefined address.
         */
        protected String getConnectionHostPort(boolean getPredefinedAddress) {
            if (getNetwork() == Network.SHARED && getPredefinedAddress)
                throw new IllegalStateException("Cannot get predefined address in shared network setup.");
            String host = defaultNetworkAlias;
            int port = applicationPort;
            if (!getPredefinedAddress) {
                host = getContainerIpAddress();
                port = getMappedPort(applicationPort);
            }
            return String.format("%s:%d", host, port);
        }
    }

    public static final class PostgresContainer extends LocatableContainer<PostgresContainer> {
        private static final String NETWORK_ALIAS = "postgres";
        private static final int POSTGRES_PORT = 5432;

        private PostgresContainer() {
            super("postgres:10-alpine", NETWORK_ALIAS, POSTGRES_PORT);
            withStartupTimeout(DEFAULT_TIMEOUT);
        }

        public String getConnectionUrl(boolean getPredefinedAddress) {
            return String.format("jdbc:postgresql://%s/postgres", getConnectionHostPort(getPredefinedAddress));
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

    public static final class KVNodeContainer extends LocatableContainer<KVNodeContainer> {
        private static final String DEFAULT_NODE_NAME = "kv-node-0";
        private static final int APPLICATION_PORT = 8080;

        private final String name;

        private KVNodeContainer(String profile, String name) {
            super("charmed-bdse-fans/bdse-kvnode:latest", name, APPLICATION_PORT);
            withEnv(Env.KVNODE_NAME, name);
            withEnv(Env.SPRING_PROFILES_ACTIVE, profile);
            withStartupTimeout(DEFAULT_TIMEOUT);

            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getRESTBaseUrl(boolean getPredefinedAddress) {
            return String.format("http://%s", getConnectionHostPort(getPredefinedAddress));
        }
    }

    public static final class AppContainer extends LocatableContainer<AppContainer> {
        private static final int APPLICATION_PORT = 8081;
        private static final String NETWORK_ALIAS = "app";

        public enum Version {
            V1, V2
        }

        private AppContainer() {
            super("charmed-bdse-fans/bdse-app:latest", NETWORK_ALIAS, APPLICATION_PORT);
            withExposedPorts(APPLICATION_PORT);
            withStartupTimeout(DEFAULT_TIMEOUT);
            withNetworkAliases(NETWORK_ALIAS);
        }

        public String getRESTBaseUrl(boolean getPredefinedAddress) {
            return String.format("http://%s", getConnectionHostPort(getPredefinedAddress));
        }
    }

    private Containers() { }
}
