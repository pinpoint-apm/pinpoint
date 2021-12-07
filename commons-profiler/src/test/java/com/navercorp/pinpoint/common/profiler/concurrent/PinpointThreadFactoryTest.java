/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.concurrent;

import org.junit.Assert;

import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class PinpointThreadFactoryTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testCreateThreadFactory() throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);

        PinpointThreadFactory pinpoint = new PinpointThreadFactory("pinpoint");
        Thread thread = pinpoint.newThread(new Runnable() {
            @Override
            public void run() {
                counter.getAndIncrement();
            }
        });
        thread.start();
        thread.join();

        Assert.assertEquals(counter.get(), 1);

        String threadName = thread.getName();
        logger.debug(threadName);
        Assert.assertTrue(threadName.startsWith("pinpoint("));
        Assert.assertTrue(threadName.endsWith(")"));

        Thread thread2 = pinpoint.newThread(new Runnable() {
            @Override
            public void run() {
            }
        });
        logger.debug(thread2.getName());

    }
}
