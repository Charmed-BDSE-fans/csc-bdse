package ru.csc.bdse.kv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.kv.node.InMemoryKeyValueApi;
import ru.csc.bdse.kv.node.InternalKeyValueApi;
import ru.csc.bdse.kv.util.Env;

@Configuration
@Profile(InMemoryKeyValueApiConfig.PROFILE)
public class InMemoryKeyValueApiConfig {
    public static final String PROFILE = "kvnode-in_memory";

    @Value("${" + Env.KVNODE_NAME_PROPERTY + ":#{null}}")
    private String nodeName;

    @Bean
    InternalKeyValueApi inMemoryNode() {
            String nodeName = this.nodeName;
            if (nodeName == null)
                nodeName = Env.randomNodeName();
        return new InMemoryKeyValueApi(nodeName);
    }
}
