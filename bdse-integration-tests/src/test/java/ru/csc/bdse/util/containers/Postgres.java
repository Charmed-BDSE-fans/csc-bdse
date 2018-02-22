package ru.csc.bdse.util.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import ru.csc.bdse.util.Env;

import java.io.File;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Postgres {

    public static GenericContainer db(Network network) {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromClasspath("kvdb.sql", "db/kvdb.sql")
                        .withFileFromClasspath("Dockerfile","db/Dockerfile")
        ).withNetwork(network)
                .withExposedPorts(5432)
                .withStartupTimeout(Duration.of(30, SECONDS));
    }

    public static GenericContainer kvnode(Network network) {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromFile("target/bdse-kvnode-0.0.1-SNAPSHOT.jar", new File
                                ("../bdse-kvnode/target/bdse-kvnode-0.0.1-SNAPSHOT.jar"))
                        .withFileFromClasspath("Dockerfile", "kvnode/Dockerfile"))
                .withNetwork(network)
                .withEnv(Env.KVNODE_NAME, "node-0")
                .withEnv(Env.SPRING_PROFILES_ACTIVE, "postgres")
                .withExposedPorts(8080)
                .withStartupTimeout(Duration.of(30, SECONDS));
    }
}
