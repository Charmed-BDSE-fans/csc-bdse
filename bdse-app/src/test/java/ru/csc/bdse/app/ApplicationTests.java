package ru.csc.bdse.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.csc.bdse.app.config.PhoneBookApiV1Config;
import ru.csc.bdse.kv.config.InMemoryKeyValueApiConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({PhoneBookApiV1Config.PROFILE, InMemoryKeyValueApiConfig.PROFILE})
public class ApplicationTests {
    @Test
    public void contextLoads() { }
}
