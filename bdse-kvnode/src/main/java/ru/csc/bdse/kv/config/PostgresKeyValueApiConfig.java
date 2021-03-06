package ru.csc.bdse.kv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.csc.bdse.kv.db.RecordRepository;
import ru.csc.bdse.kv.node.InternalKeyValueApi;
import ru.csc.bdse.kv.node.PostgresKeyValueApi;
import ru.csc.bdse.kv.util.Env;

@Configuration
@EnableJpaRepositories(basePackageClasses = RecordRepository.class)
@Profile(PostgresKeyValueApiConfig.PROFILE)
public class PostgresKeyValueApiConfig {
    public static final String PROFILE = "kvnode-postgres";

    @Value("${" + Env.KVNODE_NAME_PROPERTY + ":#{null}}")
    private String nodeName;

    @Bean
    InternalKeyValueApi postgresNode(RecordRepository repository) {
            String nodeName = this.nodeName;
            if (nodeName == null)
                nodeName = Env.randomNodeName();
        return new PostgresKeyValueApi(nodeName, repository);
    }
}