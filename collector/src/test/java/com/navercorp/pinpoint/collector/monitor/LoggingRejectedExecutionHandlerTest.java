/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.monitor;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoggingRejectedExecutionHandlerTest {

    @Test
    public void rejectedExecution() throws Exception{
        LoggingRejectedExecutionHandler handler = new LoggingRejectedExecutionHandler("testExecutor", 10);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        };

        for (int i = 0; i < 100; i++) {
            executor.execute(runnable);
            handler.rejectedExecution(runnable, executor);
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }
}