/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.lettuce;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import io.lettuce.core.AbstractRedisAsyncCommands;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-redis-lettuce-plugin")
@Dependency({"io.lettuce:lettuce-core:[5.0,]",
        "org.latencyutils:LatencyUtils:[2.0.3]",
        PluginITConstants.VERSION, TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(RedisServer.class)
@Disabled
public class RedisClient_IT {
    private static final String SERVICE_TYPE_REDIS_LETTUCE = "REDIS_LETTUCE";

    private final Logger logger = LogManager.getLogger(getClass());

    private static String host;
    private static int port;
    private static RedisClient redisClient;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        final String value = beforeAllResult.getProperty("PORT");
        port = Integer.parseInt(value);
        host = beforeAllResult.getProperty("HOST");

    }

    @BeforeAll
    public static void beforeClass() {
        String url = String.format("redis://%s:%s", host, port);
        redisClient = RedisClient.create(url);
    }

    @AfterAll
    public static void afterClass() {
        if (redisClient != null) {
            redisClient.close();
        }
    }

    @Test
    public void basic() throws Exception {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.set("foo", "bar");
        syncCommands.get("foo");
        connection.close();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        // io.lettuce.core.AbstractRedisAsyncCommands.set(java.lang.Object, java.lang.Object)
        Method setMethod = AbstractRedisAsyncCommands.class.getDeclaredMethod("set", Object.class, Object.class);
        // io.lettuce.core.AbstractRedisAsyncCommands.get(java.lang.Object)
        Method getMethod = AbstractRedisAsyncCommands.class.getDeclaredMethod("get", Object.class);
        verifier.verifyTrace(Expectations.event(SERVICE_TYPE_REDIS_LETTUCE, setMethod));
        verifier.verifyTrace(Expectations.event(SERVICE_TYPE_REDIS_LETTUCE, getMethod));
    }

    @Test
    public void async() throws Exception {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> commands = connection.async();

        RedisFuture<String> future = commands.set("foo", "bar");
        String result = future.get(1000, TimeUnit.MILLISECONDS);

        future = commands.get("foo");
        result = future.get(1000, TimeUnit.MILLISECONDS);
        connection.close();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        // io.lettuce.core.AbstractRedisAsyncCommands.set(java.lang.Object, java.lang.Object)
        Method setMethod = AbstractRedisAsyncCommands.class.getDeclaredMethod("set", Object.class, Object.class);
        // io.lettuce.core.AbstractRedisAsyncCommands.get(java.lang.Object)
        Method getMethod = AbstractRedisAsyncCommands.class.getDeclaredMethod("get", Object.class);
        verifier.verifyTrace(Expectations.event(SERVICE_TYPE_REDIS_LETTUCE, setMethod));
        verifier.verifyTrace(Expectations.event(SERVICE_TYPE_REDIS_LETTUCE, getMethod));
    }
}