package com.navercorp.pinpoint.redis.timeseries;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisServer {

    @SuppressWarnings("resource")
    public static RedisContainer newRedisServer() {
        RedisContainer container = new RedisContainer(DockerImageName.parse("redis:8.0-M02"));
        return container.withExposedPorts(6379);
    }
}
