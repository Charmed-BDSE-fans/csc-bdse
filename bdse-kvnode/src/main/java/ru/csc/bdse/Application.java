package ru.csc.bdse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.csc.bdse.db.RecordRepository;
import ru.csc.bdse.kv.InMemoryKeyValueApi;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.kv.PostgresKeyValueApi;
import ru.csc.bdse.util.Env;

import java.util.UUID;

@SpringBootApplication
@EnableJpaRepositories
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }

    @Bean
    @Profile("inmemory")
    KeyValueApi node() {
        String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Application::randomNodeName);
        return new InMemoryKeyValueApi(nodeName);
    }

    @Bean
    @Profile("postgres")
    KeyValueApi postgresNode(RecordRepository repository) {
        String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Application::randomNodeName);
        return new PostgresKeyValueApi(nodeName, repository);
    }
}
