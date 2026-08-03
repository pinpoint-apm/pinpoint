/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.redisson;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The reactive command surface funnels every call through
 * {@code org.redisson.reactive.ReactiveProxyBuilder$1.execute}, whose signature changed across
 * redisson versions — {@code (Method, Object, Object[])}, then {@code (Method, Object, Method,
 * Object[])} (~3.17.x), then {@code (Callable, Method)} (3.19+). The plugin matched only the
 * first, so reactive tracing was silently dead on modern redisson. This IT pins the weaving on
 * both current shapes, and the {@code args[0]} annotation additionally pins the keytrace Method
 * argument lookup (the Method moved to args[1] in the 3.19+ shape).
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.redisson:redisson:[3.17.7],[3.27.2]",
        PluginITConstants.VERSION})
@SharedDependency({PluginITConstants.VERSION, TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(RedisServer.class)
public class RedissonReactive_IT {
    private static final String REDISSON_REACTIVE = "REDIS_REDISSON_REACTIVE";

    private static RedissonClient redisson;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeClass() {
        final String host = System.getProperty("HOST");
        final int port = Integer.parseInt(System.getProperty("PORT"));
        final Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s", host, port));
        redisson = Redisson.create(config);
    }

    @AfterAll
    public static void afterClass() {
        if (redisson != null) {
            redisson.shutdown();
        }
    }

    /**
     * Resolve the woven execute by reflection: the owner is an anonymous class and the signature
     * is version-dependent.
     */
    private static Member reactiveExecuteMethod() throws Exception {
        final Class<?> owner = Class.forName("org.redisson.reactive.ReactiveProxyBuilder$1");
        try {
            // redisson 3.19+
            return owner.getDeclaredMethod("execute", Callable.class, Method.class);
        } catch (NoSuchMethodException e1) {
            try {
                // redisson ~3.17.x
                return owner.getDeclaredMethod("execute", Method.class, Object.class, Method.class, Object[].class);
            } catch (NoSuchMethodException e2) {
                // older
                return owner.getDeclaredMethod("execute", Method.class, Object.class, Object[].class);
            }
        }
    }

    @Test
    public void reactiveCommands_recordReactiveEvents() throws Exception {
        final RedissonReactiveClient reactive = redisson.reactive();
        final RBucketReactive<String> bucket = reactive.getBucket("foo");

        bucket.set("bar").block();
        final String value = bucket.get().block();
        assertEquals("bar", value);

        final Member execute = reactiveExecuteMethod();
        // the recorded method name depends on which Method the signature carries at the keytrace
        // position: the 3.19+ (Callable, Method) shape sees the interface method ("set"), the
        // ~3.17.x (Method, Object, Method, Object[]) shape sees the underlying async method
        // ("setAsync") at args[0].
        final boolean callableShape = ((Method) execute).getParameterTypes()[0] == Callable.class;
        final String setName = callableShape ? "set" : "setAsync";
        final String getName = callableShape ? "get" : "getAsync";

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        // discrete: the surrounding REDIS_REDISSON (sync/async surface) events are not under test.
        verifier.verifyDiscreteTrace(Expectations.event(REDISSON_REACTIVE, execute,
                Expectations.annotation("args[0]", setName)));
        verifier.verifyDiscreteTrace(Expectations.event(REDISSON_REACTIVE, execute,
                Expectations.annotation("args[0]", getName)));
    }
}
