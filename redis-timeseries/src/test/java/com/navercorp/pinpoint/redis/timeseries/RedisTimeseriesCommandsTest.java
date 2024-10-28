package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import com.navercorp.pinpoint.redis.timeseries.protocol.OnDuplicate;
import com.navercorp.pinpoint.redis.timeseries.protocol.Retention;
import com.navercorp.pinpoint.redis.timeseries.protocol.TS;
import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.IntegerOutput;
import io.lettuce.core.protocol.CommandArgs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedisTimeseriesCommandsTest {

    private static RedisContainer server = RedisServer.newRedisServer();

    private final Logger logger = LogManager.getLogger(this.getClass());

    static RedisClient client;

    RedisTimeseriesCommands commands;

    @BeforeAll
    static void beforeAll() {
        server.start();

        RedisURI redisURI = RedisURI.create(server.getRedisURI());
        client = RedisClient.create(redisURI);
    }

    @AfterAll
    static void afterAll() {
        if (client != null) {
            client.shutdown();
        }
        server.stop();
    }

    @BeforeEach
    void setUp() {
        if (client != null) {
            commands = new RedisTimeseriesCommandsImpl(client.connect());
        }
    }

    @AfterEach
    void tearDown() {
        if (commands != null) {
            commands.close();
        }
    }

    @Test
    public void ts_add() {

        RedisTimeseriesCommands commands = new RedisTimeseriesCommandsImpl(client.connect());

        TsAddArgs options = new TsAddArgs()
                .onDuplicate(OnDuplicate.last())
                .retention(Retention.of(3, TimeUnit.SECONDS));

        commands.tsAdd("test1", 1000, 1, options);
        commands.tsAdd("test1", 2000, 2, options);
        commands.tsAdd("test1", 1000, 4, options);
        TimestampValuePair tsGet = commands.tsGet("test1");
        logger.debug("tsGet:{}", tsGet);

        List<TimestampValuePair> revResult = commands.tsRevrange("test1", 0, 3000);
        logger.debug("rev:{}", revResult);
    }

    @Test
    public void timeseries_command_metric() {

        StatefulRedisConnection<String, String> connect = client.connect();

        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8).addKey("test1").add(0).add(3000);
        RedisCodec<String, String> codec = StringCodec.UTF8;

        List<TimestampValuePair> range = connect.sync().dispatch(TS.RANGE, new ArrayTimestampValueOutput<>(codec), args);
        logger.info("range:{}", range);
    }


    @Test
    public void timeseries_tsAdd() {

        StatefulRedisConnection<String, String> connect = client.connect();

        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.ASCII).addKey("test-command").add("1002").add(3);
        OnDuplicate.last().build(args);

        RedisCodec<String, String> codec = StringCodec.UTF8;
        Long timestamp = connect.sync().dispatch(TS.ADD, new IntegerOutput<>(codec), args);
        logger.info("timestamp:{}", timestamp);

    }

    private void scanAll() {
        try (StatefulRedisConnection<String, String> connect = client.connect()) {
            KeyScanCursor<String> scan = connect.sync().scan();
            for (String key : scan.getKeys()) {
                logger.info("scan key:{}", key);
            }
        }
    }
}
