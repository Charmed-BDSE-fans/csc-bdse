package ru.csc.bdse.kv.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.kv.node.InMemoryKeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.util.Env;

@SpringBootConfiguration
@Profile(InMemoryKeyValueApiConfig.PROFILE)
public class InMemoryKeyValueApiConfig {
    public static final String PROFILE = "kvnode-in_memory";

    @Bean
    KeyValueApi inMemoryNode() {
        String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Env::randomNodeName);
        return new InMemoryKeyValueApi(nodeName);
    }
}
