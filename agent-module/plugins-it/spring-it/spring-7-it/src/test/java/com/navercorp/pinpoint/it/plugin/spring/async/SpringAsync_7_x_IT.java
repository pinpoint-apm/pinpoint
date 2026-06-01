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
package com.navercorp.pinpoint.it.plugin.spring.async;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Exercises the Spring 7 async paths covered by SpringAsyncPlugin:
 * - {@code @Async} returning {@link CompletableFuture}: routed through
 *   {@code AsyncExecutionAspectSupport.doSubmit} -> {@code AsyncTaskExecutor.submitCompletable}.
 * - {@code @Async} returning {@link Future}/void: routed through {@code doSubmit} -> {@code submit}.
 * - Direct {@link ThreadPoolTaskExecutor#submitCompletable(java.util.concurrent.Callable)}:
 *   exercises the new {@code submitCompletable} interceptor in {@code AsyncTaskExecutorTransform}.
 *
 * Disabled by default to match the other spring-it tests; flip to enabled when wiring CI.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-context:[7.0.0,)", "org.springframework:spring-test"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
@Disabled
public class SpringAsync_7_x_IT {

    @Test
    public void asyncCompletableFuture() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class)) {
            AsyncService service = context.getBean(AsyncService.class);
            CompletableFuture<String> result = service.completable("ping");
            String value = result.get(5, TimeUnit.SECONDS);
            org.junit.jupiter.api.Assertions.assertEquals("ping:done", value);
        }
    }

    @Test
    public void asyncFuture() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class)) {
            AsyncService service = context.getBean(AsyncService.class);
            Future<String> result = service.future("ping");
            String value = result.get(5, TimeUnit.SECONDS);
            org.junit.jupiter.api.Assertions.assertEquals("ping:done", value);
        }
    }

    @Test
    public void asyncVoid() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class)) {
            AsyncService service = context.getBean(AsyncService.class);
            service.fireAndForget("ping");
            service.latch().await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void executorSubmitCompletable() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AsyncConfig.class)) {
            ThreadPoolTaskExecutor executor = context.getBean("taskExecutor", ThreadPoolTaskExecutor.class);
            CompletableFuture<String> result = executor.submitCompletable(() -> "direct:done");
            String value = result.get(5, TimeUnit.SECONDS);
            org.junit.jupiter.api.Assertions.assertEquals("direct:done", value);
        }
    }
}
