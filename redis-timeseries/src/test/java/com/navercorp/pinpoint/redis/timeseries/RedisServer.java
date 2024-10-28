package com.navercorp.pinpoint.redis.timeseries;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisServer {

    @SuppressWarnings("resource")
    public static RedisContainer newRedisServer() {
        return new RedisContainer(DockerImageName.parse("redis:8.0-M01"))
                .withExposedPorts(6379);
    }
}
