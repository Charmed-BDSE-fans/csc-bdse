package ru.csc.bdse.kv.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.kv.node.*;
import ru.csc.bdse.kv.util.Env;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(CoordinatedKeyValueApiConfig.Configuration.class)
public class CoordinatedKeyValueApiConfig {
    public static final String PROFILE = "kvnode-coordinated";

    @Bean
    @Primary
    @Profile(PROFILE)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public CoordinatedKeyValueApi coordinatedKeyValueApi(
            Optional<InternalKeyValueApi> localApi, Configuration cfg, ObjectMapper mapper, ConflictResolver resolver
    ) {
        List<InternalKeyValueApi> remotes = cfg.getRemotes()
                .stream()
                .map(KeyValueApiHttpClient::new)
                .collect(Collectors.toList());
        localApi.ifPresent(internalKeyValueApi -> remotes.add(0, internalKeyValueApi));
        return new CoordinatedKeyValueApi(cfg.getRcl(), cfg.getWcl(), cfg.getTimeout(), remotes, mapper, resolver);
    }

    @Bean
    public ConflictResolver defaultConflictResolver() {
        return new TimestampConflictResolver();
    }

    /**
     * @see Env class to check field names!
     */
    @ConfigurationProperties(Env.KVNODE_COORDINATION)
    public static class Configuration {
        private int rcl = 0;
        private int wcl = 0;
        private int timeout = 0;
        private List<String> remotes = Collections.emptyList();

        public int getRcl() {
            return rcl;
        }

        public void setRcl(int rcl) {
            this.rcl = rcl;
        }

        public int getWcl() {
            return wcl;
        }

        public void setWcl(int wcl) {
            this.wcl = wcl;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public List<String> getRemotes() {
            return remotes;
        }

        public void setRemotes(List<String> remotes) {
            this.remotes = remotes;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "rcl=" + rcl +
                    ", wcl=" + wcl +
                    ", timeout=" + timeout +
                    ", remotes=" + remotes +
                    '}';
        }
    }
}
