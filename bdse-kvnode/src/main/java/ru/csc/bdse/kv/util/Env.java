package ru.csc.bdse.kv.util;

import java.util.Optional;
import java.util.UUID;

/**
 * @author semkagtn
 */
public class Env {

    private Env() { }

    public static final String KVNODE_NAME_PROPERTY = "kvNode.name";

    public static final String SPRING_PROFILES_ACTIVE_PROPERTY = "spring.profiles.active";

    public static final String SPRING_DATASOURCE_URL_PROPERTY = "spring.datasource.url";

    public static final String KVNODE_BASEURL_PROPERTY = "kvNode.baseUrl";

    public static final String KVNODE_COORDINATION = "kvNode.coordination";
    public static final String KVNODE_COORDINATION_RCL = KVNODE_COORDINATION + ".rcl";
    public static final String KVNODE_COORDINATION_WCL = KVNODE_COORDINATION + ".wcl";
    public static final String KVNODE_COORDINATION_REMOTES = KVNODE_COORDINATION + ".remotes";

    public static Optional<String> get(final String name) {
        return Optional.ofNullable(System.getenv(name));
    }

    public static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }
}
