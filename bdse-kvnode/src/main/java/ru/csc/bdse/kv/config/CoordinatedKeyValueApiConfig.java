package ru.csc.bdse.kv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.kv.node.CoordinatedKeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApiHttpClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(CoordinatedKeyValueApiConfig.Configuration.class)
public class CoordinatedKeyValueApiConfig {
    public static final String PROFILE = "kvnode-coordinated";

    @Bean
    @Profile("!" + PROFILE)
    public CoordinatedKeyValueApiFactory noopCoordinatedKeyValueApiFactory() {
        return localApi -> localApi;
    }

    @Bean
    @Profile(PROFILE)
    public CoordinatedKeyValueApiFactory coordinatedKeyValueApiFactory(Configuration cfg) {
        return localApi -> {
            List<KeyValueApi> remotes = cfg.getRemotes()
                    .stream()
                    .map(KeyValueApiHttpClient::new)
                    .collect(Collectors.toList());
            System.out.println(cfg);
            remotes.add(0, localApi);
            return new CoordinatedKeyValueApi(cfg.rcl, cfg.wcl, remotes);
        };
    }

    @ConfigurationProperties("kvNode.coordination")
    public static class Configuration {
        private int rcl = 0;
        private int wcl = 0;
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
                    ", remotes=" + remotes +
                    '}';
        }
    }
}
