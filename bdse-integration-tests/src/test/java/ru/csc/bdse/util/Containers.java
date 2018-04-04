package ru.csc.bdse.util;

import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import ru.csc.bdse.app.config.PhoneBookApiV1Config;
import ru.csc.bdse.app.config.PhoneBookApiV2Config;
import ru.csc.bdse.kv.config.*;
import ru.csc.bdse.kv.util.Env;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Container utils for testing
 */
@SuppressWarnings("WeakerAccess")
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
        return kvNode(new KVNodeContainer.InMemory(name));
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
        return kvNode(new KVNodeContainer.Postgres(name, connectionUrl));
    }

    /**
     * Creates Postgres-based key-value node with default name.
     * @param connectionUrl Database connection URL.
     */
    public static KVNodeContainer postgresNode(String connectionUrl) {
        return postgresNode(KVNodeContainer.DEFAULT_NODE_NAME, connectionUrl);
    }

    /**
     * Creates kvNode with given config and default name
     * @param config config
     */
    public static KVNodeContainer kvNode(KVNodeContainer.Config config) {
        String name = null;
        if (config instanceof KVNodeContainer.InMemory)
            name = ((KVNodeContainer.InMemory) config).name;
        if (config instanceof KVNodeContainer.Postgres)
            name = ((KVNodeContainer.Postgres) config).name;
        KVNodeContainer container = new KVNodeContainer(name);
        return configureKVNode(container, config);
    }

    private static <T extends SpringAppContainer<T>> T configureKVNode(
            T container,
            KVNodeContainer.Config config
    ) {
        if (config instanceof KVNodeContainer.InMemory) {
            return container
                    .withSpringProfile(InMemoryKeyValueApiConfig.PROFILE);
        }
        if (config instanceof KVNodeContainer.Postgres) {
            return container
                    .withSpringProfile(PostgresKeyValueApiConfig.PROFILE)
                    .withSpringProperty(Env.SPRING_DATASOURCE_URL_PROPERTY, ((KVNodeContainer.Postgres) config).connectionUrl);
        }
        if (config instanceof KVNodeContainer.Remote) {
            return container
                    .withSpringProfile(KeyValueApiHttpClientConfig.PROFILE)
                    .withSpringProperty(Env.KVNODE_BASEURL_PROPERTY, ((KVNodeContainer.Remote) config).baseUrl);
        }
        if (config instanceof KVNodeContainer.Coordinated) {
            KVNodeContainer.Coordinated coordinated = (KVNodeContainer.Coordinated) config;
            if (coordinated.localConfig != null) {
                container = configureKVNode(container, coordinated.localConfig);
            }
            return container
                    .withSpringProfile(CoordinatedKeyValueApiConfig.PROFILE)
                    .withSpringProperty(Env.KVNODE_COORDINATION_RCL, Integer.toString(coordinated.rcl))
                    .withSpringProperty(Env.KVNODE_COORDINATION_WCL, Integer.toString(coordinated.wcl))
                    .withSpringProperty(Env.KVNODE_COORDINATION_TIMEOUT, Integer.toString(coordinated.timeout))
                    .withSpringProperty(Env.KVNODE_COORDINATION_REMOTES, String.join(",", coordinated.remotes));
        }
        throw new IllegalArgumentException("KVNodeContainer.Config");
    }

    /**
     * Coordinate given KVNodes to work together as a cluster
     * @param containers list of containers to configure
     * @param rcl read consistency level
     * @param wcl write consisiteny level
     * @param timeout timeout in millis
     */
    public static <T extends KVNodeContainerBase<T>> void coordinateKvNodes(
            List<T> containers,
            int rcl,
            int wcl,
            int timeout
    ) {
        for (T container: containers) {
            List<String> remotes = containers.stream()
                    .filter(t -> t != container)
                    .map(t -> t.getInternalKVBaseUrl(true))
                    .collect(Collectors.toList());
            configureKVNode(container, new KVNodeContainer.Coordinated(rcl, wcl, timeout, remotes, null));
        }
    }

    /**
     * Creates app container which runs on remote kvNode
     * @param version PhoneBookApi version
     * @param baseUrl KvNode connection Url
     */
    public static AppContainer applicationWithRemoteKV(AppContainer.Version version, String baseUrl) {
        return application(version, new KVNodeContainer.Remote(baseUrl));
    }

    /**
     * Creates app container which runs on required kvNode
     * @param version PhoneBookApi version
     * @param config KvNode config to use
     */
    public static AppContainer application(AppContainer.Version version, KVNodeContainer.Config config) {
        return configureKVNode(configureApp(new AppContainer(), version), config);
    }

    private static <T extends SpringAppContainer<T>> T configureApp(T container, AppContainer.Version version) {
        switch (version) {
            case V1:
                return container.withSpringProfile(PhoneBookApiV1Config.PROFILE);
            case V2:
                return container.withSpringProfile(PhoneBookApiV2Config.PROFILE);
        }
        throw new IllegalArgumentException("AppContainer.Version");
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

    private static abstract class SpringAppContainer<T extends SpringAppContainer<T>> extends LocatableContainer<T> {
        private static final int APPLICATION_PORT = 8080;

        private final List<String> profiles = new ArrayList<>();

        public static String springPropertyToEnv(String property) {
            return property.toUpperCase().replace('.', '_');
        }

        protected SpringAppContainer(String image, String defaultNetworkAlias) {
            super(image, defaultNetworkAlias, APPLICATION_PORT);
        }

        public T withSpringProfile(String profile) {
            profiles.add(profile);
            return withSpringProperty(Env.SPRING_PROFILES_ACTIVE_PROPERTY, String.join(",", profiles));
        }

        public T withSpringProperty(String property, String value) {
            return withEnv(springPropertyToEnv(property), value);
        }

        public List<String> getSpringProfiles() {
            return Collections.unmodifiableList(profiles);
        }
    }

    public static abstract class KVNodeContainerBase<T extends KVNodeContainerBase<T>> extends SpringAppContainer<T> {
        protected KVNodeContainerBase(String image, String defaultNetworkAlias) {
            super(image, defaultNetworkAlias);
        }

        public String getKVBaseUrl(boolean getPredefinedAddress) {
            return String.format("http://%s", getConnectionHostPort(getPredefinedAddress));
        }

        public String getInternalKVBaseUrl(boolean getPredefinedAddress) {
            return String.format("http://%s/%s", getConnectionHostPort(getPredefinedAddress), KeyValueApiControllerConfig.INTERNAL_KV_PREFIX);
        }
    }

    public static final class KVNodeContainer extends KVNodeContainerBase<KVNodeContainer> {
        private static final String DEFAULT_NODE_NAME = "kv-node-0";

        public interface Config { }

        public static class InMemory implements Config {
            private final String name;

            public InMemory(String name) {
                this.name = name;
            }
        }

        public static class Postgres implements Config {
            private final String name;
            public final String connectionUrl;

            private Postgres(String name, String connectionUrl) {
                this.name = name;
                this.connectionUrl = connectionUrl;
            }
        }

        public static class Remote implements Config {
            public final String baseUrl;

            private Remote(String baseUrl) {
                this.baseUrl = baseUrl;
            }
        }

        public static class Coordinated implements Config {
            public final int rcl, wcl, timeout;
            public final List<String> remotes;
            public final Config localConfig;

            public Coordinated(int rcl, int wcl, int timeout, List<String> remotes, Config localConfig) {
                this.rcl = rcl;
                this.wcl = wcl;
                this.timeout = timeout;
                this.remotes = remotes;
                this.localConfig = localConfig;
            }
        }

        private final String name;

        private KVNodeContainer(String name) {
            super("charmed-bdse-fans/bdse-kvnode:latest", name);
            withStartupTimeout(DEFAULT_TIMEOUT);
            if (name != null) {
                withSpringProperty(Env.KVNODE_NAME_PROPERTY, name);
            }

            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class AppContainer extends KVNodeContainerBase<AppContainer> {
        private static final String NETWORK_ALIAS = "app";

        public enum Version {
            V1, V2
        }

        private AppContainer() {
            super("charmed-bdse-fans/bdse-app:latest", NETWORK_ALIAS);
            withStartupTimeout(DEFAULT_TIMEOUT);
        }

        public String getAppBaseUrl(boolean getPredefinedAddress) {
            return String.format("http://%s", getConnectionHostPort(getPredefinedAddress));
        }
    }

    private Containers() { }
}
