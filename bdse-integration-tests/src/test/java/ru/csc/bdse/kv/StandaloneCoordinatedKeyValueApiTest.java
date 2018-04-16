package ru.csc.bdse.kv;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.csc.bdse.kv.config.CoordinatedKeyValueApiConfig;
import ru.csc.bdse.kv.config.InMemoryKeyValueApiConfig;
import ru.csc.bdse.kv.node.KeyValueApi;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({InMemoryKeyValueApiConfig.PROFILE, CoordinatedKeyValueApiConfig.PROFILE})
public class StandaloneCoordinatedKeyValueApiTest extends AbstractKeyValueApiTest {
    @Autowired
    private KeyValueApi api;

    @Override
    protected KeyValueApi newKeyValueApi() {
        return api;
    }
}
