package ru.csc.bdse.kv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.kv.node.InternalKeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApiHttpClient;
import ru.csc.bdse.kv.util.Env;

@Configuration
@Profile(KeyValueApiHttpClientConfig.PROFILE)
public class KeyValueApiHttpClientConfig {
    public static final String PROFILE = "kvnode-client";

    @Value("${" + Env.KVNODE_BASEURL_PROPERTY + "}")
    private String keyValueBaseUrl;

    @Bean
    public InternalKeyValueApi keyValueApiHttpClient() {
        return new KeyValueApiHttpClient(keyValueBaseUrl);
    }
}
