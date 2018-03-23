package ru.csc.bdse.kv.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.csc.bdse.kv.controller.KeyValueApiController;

@Configuration
@ComponentScan(basePackageClasses = KeyValueApiController.class)
public class KeyValueApiControllerConfig {
    /*
     * This is just a "proxy" configuration
     */
}