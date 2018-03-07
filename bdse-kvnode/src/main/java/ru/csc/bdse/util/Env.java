package ru.csc.bdse.util;

import java.util.Optional;
import java.util.UUID;

/**
 * @author semkagtn
 */
public class Env {

    private Env() {

    }

    private static String springPropertyToEnv(String property) {
        return property.toUpperCase().replace('.', '_');
    }

    public static final String KVNODE_NAME = "KVNODE_NAME";

    public static final String SPRING_PROFILES_ACTIVE_PROPERTY = "spring.profiles.active";
    public static final String SPRING_PROFILES_ACTIVE = springPropertyToEnv(SPRING_PROFILES_ACTIVE_PROPERTY);

    public static final String SPRING_DATASOURCE_URL_PROPERTY = "spring.datasource.url";
    public static final String SPRING_DATASOURCE_URL = springPropertyToEnv(SPRING_DATASOURCE_URL_PROPERTY);

    public static Optional<String> get(final String name) {
        return Optional.ofNullable(System.getenv(name));
    }

    public static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }
}
