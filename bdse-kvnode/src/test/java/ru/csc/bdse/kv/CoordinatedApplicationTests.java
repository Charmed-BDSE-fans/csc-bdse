package ru.csc.bdse.kv;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.csc.bdse.kv.config.CoordinatedKeyValueApiConfig;
import ru.csc.bdse.kv.config.InMemoryKeyValueApiConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({InMemoryKeyValueApiConfig.PROFILE, CoordinatedKeyValueApiConfig.PROFILE})
public class CoordinatedApplicationTests {
    @Test
    public void contextLoads() {
    }
}