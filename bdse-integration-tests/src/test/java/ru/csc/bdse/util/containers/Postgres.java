package ru.csc.bdse.util;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Containers {

    public static GenericContainer postgres() {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromClasspath("kvdb.sql", "db/kvdb.sql")
                        .withFileFromClasspath("Dockerfile","db/Dockerfile")
        ).withExposedPorts(5432)
                .withStartupTimeout(Duration.of(30, SECONDS));
    }

    public static GenericContainer postgreskvnode() {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withFileFromFile("target/bdse-kvnode-0.0.1-SNAPSHOT.jar", new File
                                ("../bdse-kvnode/target/bdse-kvnode-0.0.1-SNAPSHOT.jar"))
                        .withFileFromClasspath("Dockerfile", "kvnode/Dockerfile"))
                .withEnv(Env.KVNODE_NAME, "node-0")
                .withEnv(Env.SPRING_PROFILES_ACTIVE, "postgres")
                .withExposedPorts(8080)
                .withStartupTimeout(Duration.of(30, SECONDS));
    }
}
