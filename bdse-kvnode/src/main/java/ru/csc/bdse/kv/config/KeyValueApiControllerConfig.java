package ru.csc.bdse.kv.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.kv.controller.KeyValueApiControllerBase;
import ru.csc.bdse.kv.node.CoordinatedKeyValueApi;
import ru.csc.bdse.kv.node.InternalKeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApi;

@Configuration
@ComponentScan(basePackageClasses = KeyValueApiControllerBase.class)
public class KeyValueApiControllerConfig {
    public static final String INTERNAL_KV_PREFIX = "/internal/";

    @RestController
    @Profile("!" + CoordinatedKeyValueApiConfig.PROFILE)
    public class KeyValueApiController extends KeyValueApiControllerBase {
        public KeyValueApiController(KeyValueApi keyValueApi) {
            super(keyValueApi);
        }
    }

    @RestController
    @RequestMapping(INTERNAL_KV_PREFIX)
    @Profile(CoordinatedKeyValueApiConfig.PROFILE)
    public class InternalKeyValueApiController extends KeyValueApiControllerBase {
        public InternalKeyValueApiController(InternalKeyValueApi keyValueApi) {
            super(keyValueApi);
        }
    }

    @RestController
    @Profile(CoordinatedKeyValueApiConfig.PROFILE)
    public class CoordinatedKeyValueApiController extends KeyValueApiControllerBase {
        public CoordinatedKeyValueApiController(CoordinatedKeyValueApi keyValueApi) {
            super(keyValueApi);
        }
    }
}