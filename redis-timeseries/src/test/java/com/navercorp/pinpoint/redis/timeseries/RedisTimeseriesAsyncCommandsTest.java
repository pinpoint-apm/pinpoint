package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.connection.AsyncConnection;
import com.navercorp.pinpoint.redis.timeseries.connection.ClusterAsyncConnection;
import com.navercorp.pinpoint.redis.timeseries.connection.SimpleAsyncConnection;
import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import com.navercorp.pinpoint.redis.timeseries.protocol.OnDuplicate;
import com.navercorp.pinpoint.redis.timeseries.protocol.Retention;
import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RedisTimeseriesAsyncCommandsTest {

    @AutoClose
    private static RedisContainer server;

    private final Logger logger = LogManager.getLogger(this.getClass());

    static RedisClient client;
    static RedisClusterClient clusterClient;

    AsyncConnection<String, String> connection;

    RedisTimeseriesAsyncCommandsImpl commands;

    @BeforeAll
    static void beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        server = RedisServer.newRedisServer();
        server.start();
        RedisURI redisURI = RedisURI.create(server.getRedisURI());
        client = RedisClient.create(redisURI);

//        RedisClusterClient.create(redisURI);
//        clusterClient = RedisClusterClient.create(redisURI);
//        final RedisClusterNode node = new RedisClusterNode();
//        node.setUri(redisURI);
//        node.setSlots(IntStream.range(1, 2).boxed().collect(Collectors.toList()));
//
//        final Partitions partitions = new Partitions();
//        partitions.add(node);
//
//        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
//                .enablePeriodicRefresh(10, TimeUnit.MINUTES)
//                .build();
//
//        clusterClient.setOptions(ClusterClientOptions.builder()
//                .topologyRefreshOptions(topologyRefreshOptions)
//                .build());
    }

    @AfterAll
    static void afterAll() {
        if (client != null) {
            client.close();
        }
        if (clusterClient != null) {
            clusterClient.close();
        }
    }

    @AfterEach
    void tearDown() {
        if (connection != null) {
            connection.close();
        }
    }

    @BeforeEach
    void setUp() {
        if (client != null) {
            this.connection = new SimpleAsyncConnection<>(client.connect());
        } else {
            this.connection = new ClusterAsyncConnection<>(clusterClient.connect());
        }
        this.commands = new RedisTimeseriesAsyncCommandsImpl(connection);
    }


    @Test
    public void ts_add() throws ExecutionException, InterruptedException {
        TsAddArgs options = new TsAddArgs()
                .onDuplicate(OnDuplicate.last())
                .retention(Retention.of(3, TimeUnit.SECONDS));

        RedisFuture<Long> f1 = commands.tsAdd("test1", 1000, 1, options);
        RedisFuture<Long> f2 = commands.tsAdd("test1", 2000, 2, options);
        RedisFuture<Long> f3 = commands.tsAdd("test1", 1000, 4, options);
        Long l1 = LettuceFutures.awaitOrCancel(f1, 100, TimeUnit.MILLISECONDS);
        Long l2 = LettuceFutures.awaitOrCancel(f2, 100, TimeUnit.MILLISECONDS);
        Long l3 = LettuceFutures.awaitOrCancel(f3, 100, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(1000, l1);
        Assertions.assertEquals(2000, l2);
        Assertions.assertEquals(1000, l3);
    }


    @Test
    public void ts_range() throws ExecutionException, InterruptedException {
        TsAddArgs options = new TsAddArgs()
                .onDuplicate(OnDuplicate.last())
                .retention(Retention.of(3, TimeUnit.SECONDS));

        RedisFuture<Long> f1 = commands.tsAdd("test1", 1000, 1, options);
        RedisFuture<Long> f2 = commands.tsAdd("test1", 2000, 2, options);
        RedisFuture<Long> f3 = commands.tsAdd("test1", 3000, 3, options);

        Assertions.assertTrue(LettuceFutures.awaitAll(1000L, TimeUnit.MILLISECONDS, f1, f2, f3));

        RedisFuture<List<TimestampValuePair>> timeFuture = commands.tsRange("test1", 0, 3000);
        List<TimestampValuePair> timestampValuePairs = timeFuture.get();
        Assertions.assertEquals(3, timestampValuePairs.size());
        Assertions.assertEquals(1, timestampValuePairs.get(0).value());

        RedisFuture<TimestampValuePair> test1 = commands.tsGet("test1");
        TimestampValuePair pair = LettuceFutures.awaitOrCancel(test1, 1000, TimeUnit.MILLISECONDS);

        logger.warn("Pair {}", pair);
    }

}
