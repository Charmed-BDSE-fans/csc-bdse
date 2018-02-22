package ru.csc.bdse.util;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;
import static ru.csc.bdse.kv.KeyValueApiHttpClientTest2.testNetwork;

public class Containers {

    public static GenericContainer postgres() {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromClasspath("kvdb.sql", "db/kvdb.sql")
                        .withFileFromClasspath("Dockerfile","db/Dockerfile")
        ).withNetwork(testNetwork)
                .withExposedPorts(5432);
    }

    public static GenericContainer kvnode() {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromFile("target/bdse-kvnode-0.0.1-SNAPSHOT.jar", new File
                                ("../bdse-kvnode/target/bdse-kvnode-0.0.1-SNAPSHOT.jar"))
                        .withFileFromClasspath("Dockerfile", "kvnode/Dockerfile"))
                .withNetwork(testNetwork)
                .withEnv(Env.KVNODE_NAME, "node-0")
                .withExposedPorts(8080)
                .withStartupTimeout(Duration.of(30, SECONDS));
    }
}
