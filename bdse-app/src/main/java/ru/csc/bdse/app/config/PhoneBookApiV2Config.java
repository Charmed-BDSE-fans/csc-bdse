package ru.csc.bdse.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.csc.bdse.app.common.PhoneBookApi;
import ru.csc.bdse.app.v2.phonebook.KeyValuePhoneBookApi;
import ru.csc.bdse.app.v2.phonebook.PhoneBookRecord;
import ru.csc.bdse.kv.node.KeyValueApi;

@Configuration
@Profile(PhoneBookApiV2Config.PROFILE)
public class PhoneBookApiV2Config {
    public static final String PROFILE = "app-v2";

    @Bean
    public PhoneBookApi<PhoneBookRecord> phoneBookApiV2(KeyValueApi keyValueApi) {
        return new KeyValuePhoneBookApi(keyValueApi);
    }
}
