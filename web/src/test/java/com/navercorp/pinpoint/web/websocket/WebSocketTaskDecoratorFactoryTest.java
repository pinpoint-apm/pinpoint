/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.common.server.task.TaskDecoratorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author HyunGil Jeong
 */
public class WebSocketTaskDecoratorFactoryTest {

    private final TaskDecoratorFactory taskDecoratorFactory = new WebSocketTaskDecoratorFactory();

    @Test
    public void testAuthenticationPropagation() {
        final int numThreads = 3;
        final List<Authentication> authentications = sampleAuthentications(numThreads);
        List<CompletableFuture<Authentication>> result = new ArrayList<>();
        for (Authentication authentication : authentications) {
            CompletableFuture<Authentication> future = CompletableFuture.supplyAsync(() -> {
                SecurityContext securityContext = new SecurityContextImpl();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);
                TestTask run = new TestTask();
                Runnable task = taskDecoratorFactory.createDecorator().decorate(run);
                task.run();
                return run.result();
            });
            result.add(future);
        }

        int i = 0;
        for (Authentication authentication : authentications) {
            Authentication actual = result.get(i++).join();
            Assertions.assertEquals(authentication, actual);
        }
    }

    private List<Authentication> sampleAuthentications(int numThreads) {
        final List<Authentication> result = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            final String principal = "principal" + i;
            final String credential = "credential" + i;
            result.add(new TestingAuthenticationToken(principal, credential));
        }
        return result;
    }

    private static class TestTask implements Runnable {

        private Authentication result;

        private TestTask() {
        }

        @Override
        public void run() {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            Assertions.assertNotNull(securityContext);
            this.result = securityContext.getAuthentication();
        }

        public Authentication result() {
            return result;
        }
    }
}
