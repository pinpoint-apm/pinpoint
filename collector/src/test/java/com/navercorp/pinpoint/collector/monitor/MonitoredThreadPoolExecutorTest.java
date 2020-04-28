/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor;

import com.codahale.metrics.MetricRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MonitoredThreadPoolExecutorTest {
    private RunnableDecorator runnableDecorator;
    private MonitoredThreadPoolExecutor threadPoolExecutor;

    @Before
    public void setUp() throws Exception {
        MetricRegistry metricRegistry = new MetricRegistry();
        this.runnableDecorator = spy(new MonitoredRunnableDecorator("test", metricRegistry));
        this.threadPoolExecutor = new MonitoredThreadPoolExecutor(1, 1, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), runnableDecorator);
    }

    @After
    public void tearDown() throws Exception {
        if (this.threadPoolExecutor != null) {
            this.threadPoolExecutor.shutdown();
        }
    }

    @Test
    public void submit_runnable() {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {

            }
        });

        verify(runnableDecorator, times(1)).decorate(any(Runnable.class));
    }

    @Test
    public void submit_callable() {
        threadPoolExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });

        verify(runnableDecorator, times(1)).decorate(any(Runnable.class));
    }

    @Test
    public void submit_runnable_futureTask() {
        Future<Object> submit = threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
            }
        }, new Object());

        verify(runnableDecorator, times(1)).decorate(any(Runnable.class));
    }
}