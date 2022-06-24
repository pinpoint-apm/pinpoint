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

package com.navercorp.pinpoint.web.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class SecurityContextPropagatingTaskDecoratorTest {

    private final SecurityContextPropagatingTaskDecorator decorator = new SecurityContextPropagatingTaskDecorator();
    private final SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("Test-Worker-");

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        executor.setTaskDecorator(decorator);
    }

    @Test
    public void securityContextShouldBePropagated() throws InterruptedException {
        // Given
        final int testCount = 100;
        final CountDownLatch completeLatch = new CountDownLatch(testCount);
        final AtomicBoolean verifiedFlag = new AtomicBoolean(true);
        final TestWorker.Callback workerCallback = new TestWorker.Callback() {
            @Override
            public void onRun() {
                SecurityContext actualSecurityContext = SecurityContextHolder.getContext();
                boolean verified = securityContext == actualSecurityContext;
                verifiedFlag.compareAndSet(true, verified);
            }

            @Override
            public void onError() {
                // do nothing
            }
        };
        // When
        SecurityContextHolder.setContext(securityContext);
        for (int i = 0; i < testCount; i++) {
            executor.execute(new TestWorker(completeLatch, workerCallback));
        }
        completeLatch.await(5, TimeUnit.SECONDS);
        // Then
        boolean testVerified = verifiedFlag.get();
        Assertions.assertTrue(testVerified, "SecurityContext has not been propagated");
    }
}
