package ru.csc.bdse.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.kv.InMemoryKeyValueApi;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.util.Env;

@SpringBootConfiguration
@Profile(InMemoryKeyValueApiConfig.PROFILE)
public class InMemoryKeyValueApiConfig {
    public static final String PROFILE = "in_memory";

    @Bean
    KeyValueApi inMemoryNode() {
        String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Env::randomNodeName);
        return new InMemoryKeyValueApi(nodeName);
    }
}
