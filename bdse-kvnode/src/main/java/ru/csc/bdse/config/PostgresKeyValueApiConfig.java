package ru.csc.bdse.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.csc.bdse.db.RecordRepository;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.PostgresKeyValueApi;
import ru.csc.bdse.util.Env;

@SpringBootConfiguration
@EnableJpaRepositories(basePackageClasses = RecordRepository.class)
@Profile(PostgresKeyValueApiConfig.PROFILE)
public class PostgresKeyValueApiConfig {
    public static final String PROFILE = "postgres";

    @Bean
    KeyValueApi postgresNode(RecordRepository repository) {
        String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Env::randomNodeName);
        return new PostgresKeyValueApi(nodeName, repository);
    }
}