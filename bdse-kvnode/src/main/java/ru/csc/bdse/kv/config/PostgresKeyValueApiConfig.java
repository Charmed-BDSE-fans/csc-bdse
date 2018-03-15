package ru.csc.bdse.kv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.csc.bdse.kv.db.RecordRepository;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.node.PostgresKeyValueApi;
import ru.csc.bdse.kv.util.Env;

@Configuration
@EnableJpaRepositories(basePackageClasses = RecordRepository.class)
@Profile(PostgresKeyValueApiConfig.PROFILE)
public class PostgresKeyValueApiConfig {
    public static final String PROFILE = "kvnode-postgres";

    @Bean
    KeyValueApi postgresNode(RecordRepository repository) {
        String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Env::randomNodeName);
        return new PostgresKeyValueApi(nodeName, repository);
    }
}