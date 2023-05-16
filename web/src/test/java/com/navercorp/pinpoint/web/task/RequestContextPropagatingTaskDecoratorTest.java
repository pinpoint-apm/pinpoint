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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class RequestContextPropagatingTaskDecoratorTest {

    private final RequestContextPropagatingTaskDecorator decorator = new RequestContextPropagatingTaskDecorator();
    private final SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("Test-Worker-");

    @Mock
    private RequestAttributes requestAttributes;

    @BeforeEach
    public void setup() {
        executor.setTaskDecorator(decorator);
    }

    @Test
    public void requestContextShouldBePropagated() throws InterruptedException {
        // Given
        final int testCount = 100;
        final CountDownLatch completeLatch = new CountDownLatch(testCount);
        final AtomicBoolean verifiedFlag = new AtomicBoolean(true);
        final TestWorker.Callback workerCallback = new TestWorker.Callback() {
            @Override
            public void onRun() {
                RequestAttributes actualRequestAttributes = RequestContextHolder.getRequestAttributes();
                boolean verified = requestAttributes == actualRequestAttributes;
                verifiedFlag.compareAndSet(true, verified);
            }

            @Override
            public void onError() {
                // do nothing
            }
        };
        // When
        RequestContextHolder.setRequestAttributes(requestAttributes);
        for (int i = 0; i < testCount; i++) {
            executor.execute(new TestWorker(completeLatch, workerCallback));
        }
        completeLatch.await(5, TimeUnit.SECONDS);
        // Then
        boolean testVerified = verifiedFlag.get();
        Assertions.assertTrue(testVerified, "RequestContext has not been propagated");
    }
}
