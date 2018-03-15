package ru.csc.bdse.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.KeyValuePhoneBookApi;
import ru.csc.bdse.app.v1.phonebook.PhoneBookRecord;
import ru.csc.bdse.kv.node.KeyValueApi;

@Configuration
@Profile(PhoneBookApiV1Config.PROFILE)
public class PhoneBookApiV1Config {
    public static final String PROFILE = "app-v1";

    @Bean
    public PhoneBookApi<PhoneBookRecord> phoneBookApiV1(KeyValueApi keyValueApi) {
        return new KeyValuePhoneBookApi(keyValueApi);
    }
}
