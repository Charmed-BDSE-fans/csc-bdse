package ru.csc.bdse.kv;

import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.wait.WaitAllStrategy;
import org.testcontainers.containers.wait.WaitStrategy;
import ru.csc.bdse.kv.node.KeyValueApi;
import ru.csc.bdse.kv.node.KeyValueApiHttpClient;
import ru.csc.bdse.util.Containers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class CoordinatedKeyValueApiTest extends AbstractCoordinatedKeyValueApiTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // 1 - rf
                // 2 - wcl
                // 3 - rcl
                // 4 - failures
                { 1, 1, 1, 0 },
                { 3, 3, 1, 1 },
                { 3, 2, 3, 1 },
                { 5, 3, 3, 1 },
                { 5, 3, 3, 2 },
        });
    }

    private final List<Containers.KVNodeContainer> nodes;

    private final static int TIMEOUT = 2000;
    private final List<String> names;
    private final List<String> offNames;

    public CoordinatedKeyValueApiTest(int rf, int wcl, int rcl, int fails) {
        names = IntStream.range(0, rf)
                .boxed()
                .map(d -> String.format("kv-%d", d))
                .collect(Collectors.toList());

        offNames = names.subList(1, 1+fails);

        nodes = names.stream()
                .map(Containers::inMemoryNode)
                .collect(Collectors.toList());

        Containers.coordinateKvNodes(nodes, rcl, wcl, TIMEOUT);
    }

    @Rule
    public final ExternalResource externalResource = new ExternalResource() {
        @Override
        public Statement apply(Statement base, Description description) {
            return super.apply(base, description);
        }

        @Override
        protected void before() throws Throwable {
            for (Containers.KVNodeContainer n : nodes) {
                n.start();
                WaitStrategy ws = new WaitAllStrategy();
                ws.waitUntilReady(n);
                n.waitingFor(ws);
            }
        }

        @Override
        protected void after() {
            for (Containers.KVNodeContainer n : nodes) {
                n.stop();
            }
        }
    };


    @Override
    protected KeyValueApi getApi() {
        return new KeyValueApiHttpClient(nodes.get(0).getKVBaseUrl(false));
    }

    @Override
    protected List<String> nodesToBeOff() {
        return offNames;
    }

}