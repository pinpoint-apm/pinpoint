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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HyunGil Jeong
 */
public class ChainedTaskDecoratorTest {

    private SimpleAsyncTaskExecutor executor;

    @Before
    public void setup() {
        executor = new SimpleAsyncTaskExecutor("Test-Worker-");
    }

    @Test
    public void chainedDecoratorsShouldBeCalled() throws InterruptedException {
        // Given
        final int testCount = 100;
        final CountDownLatch completeLatch = new CountDownLatch(testCount);
        final CountingTaskDecorator decorator1 = new CountingTaskDecorator();
        final CountingTaskDecorator decorator2 = new CountingTaskDecorator();
        final CountingTaskDecorator decorator3 = new CountingTaskDecorator();
        final List<TaskDecorator> decorators = Arrays.asList(decorator1, decorator2, decorator3);
        final ChainedTaskDecorator chainedDecorator = new ChainedTaskDecorator(decorators);
        executor.setTaskDecorator(chainedDecorator);
        // When
        for (int i = 0; i < testCount; i++) {
            executor.execute(new TestWorker(completeLatch));
        }
        completeLatch.await(5L, TimeUnit.SECONDS);
        // Then
        Assert.assertEquals(testCount, decorator1.getCount());
        Assert.assertEquals(testCount, decorator2.getCount());
        Assert.assertEquals(testCount, decorator3.getCount());
    }

    private static class CountingTaskDecorator implements TaskDecorator {

        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public Runnable decorate(Runnable runnable) {
            count.incrementAndGet();
            return runnable;
        }

        public int getCount() {
            return count.get();
        }
    }
}
