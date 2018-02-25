package ru.csc.bdse.util;

import java.util.Optional;
import java.util.UUID;

/**
 * @author semkagtn
 */
public class Env {

    private Env() {

    }

    public static final String KVNODE_NAME = "KVNODE_NAME";

    public static final String SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE";

    public static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

    public static Optional<String> get(final String name) {
        return Optional.ofNullable(System.getenv(name));
    }

    public static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }
}
