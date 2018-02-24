package ru.csc.bdse.util;

import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Containers {
    private static final int APPLICATION_PORT = 8080;
    private static final int POSTGRES_PORT = 5432;

    public static GenericContainer postgres(Network network) {
        return new GenericContainer(new ImageFromDockerfile()
                        .withFileFromClasspath("kvdb.sql", "db/kvdb.sql")
                        .withFileFromClasspath("Dockerfile","db/Dockerfile"))
                .withNetwork(network)
                .withExposedPorts(POSTGRES_PORT)
                .withStartupTimeout(Duration.of(30, SECONDS));
    }

    public static GenericContainer kvnode(Network network, String profile) {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromFile("target/bdse-kvnode-0.0.1-SNAPSHOT.jar", new File
                                ("../bdse-kvnode/target/bdse-kvnode-0.0.1-SNAPSHOT.jar"))
                        .withFileFromClasspath("Dockerfile", "kvnode/Dockerfile"))
                .withNetwork(network)
                .withEnv(Env.KVNODE_NAME, "node-0")
                .withEnv(Env.SPRING_PROFILES_ACTIVE, profile)
                .withExposedPorts(APPLICATION_PORT)
                .withStartupTimeout(Duration.of(30, SECONDS));
    }

    public static String getPostgresJdbc(GenericContainer container) {
        return String.format("jdbc:postgresql://localhost:%d/postgres", container.getMappedPort(POSTGRES_PORT));
    }

    public static String getKVNodeBaseUrl(GenericContainer container) {
        return String.format("http://localhost:%d", container.getMappedPort(APPLICATION_PORT));
    }

    public static void setupSpringContextForPostgres(ConfigurableApplicationContext context, GenericContainer db) {
        EnvironmentTestUtils.addEnvironment(context,
                String.format("spring.datasource.url=%s", getPostgresJdbc(db)));

    }
}
