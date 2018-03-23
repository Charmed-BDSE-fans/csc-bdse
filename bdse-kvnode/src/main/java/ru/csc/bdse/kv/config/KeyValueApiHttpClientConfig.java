package ru.csc.bdse.kv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApiHttpClient;

@Configuration
@Profile(KeyValueApiHttpClientConfig.PROFILE)
public class KeyValueApiHttpClientConfig {
    public static final String PROFILE = "kvnode-client";

    @Value("${kvNode.baseUrl}")
    private String keyValueBaseUrl;

    @Bean
    public KeyValueApi keyValueApiHttpClient(CoordinatedKeyValueApiFactory coordinatedFactory) {
        return coordinatedFactory.coordinateWithLocal(new KeyValueApiHttpClient(keyValueBaseUrl));
    }
}
