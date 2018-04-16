package ru.csc.bdse.app.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.csc.bdse.kv.config.KeyValueApiHttpClientConfig;

@Configuration
@ComponentScan(basePackageClasses = KeyValueApiHttpClientConfig.class)
public class KeyValueApiConfig {
    /*
     * This is just a "proxy" configuration
     */
}
