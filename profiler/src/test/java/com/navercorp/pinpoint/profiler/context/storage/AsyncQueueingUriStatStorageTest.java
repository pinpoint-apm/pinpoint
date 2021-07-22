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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.EachUriStatData;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;

/**
 * @author Taejin Koo
 */
public class AsyncQueueingUriStatStorageTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String[] URI_EXAMPLES = {"/index.html", "/main", "/error"};

    @Test
    public void storageTest() {
        int collectInterval = 100;
        int storeCount = RANDOM.nextInt(5) + 1;

        AsyncQueueingUriStatStorage storage = null;
        try {
            storage = new AsyncQueueingUriStatStorage(5012, 3, "Test-Executor", collectInterval);

            long sleepTime = System.currentTimeMillis() % collectInterval;

            try {
                Thread.sleep(sleepTime + 2);
            } catch (InterruptedException e) {
            }

            for (int i = 0; i < storeCount; i++) {
                storeRandomValue(storage);
            }

            Assert.assertNull(storage.poll());

            try {
                Thread.sleep(collectInterval);
            } catch (InterruptedException e) {
            }

            storage.pollTimeout(collectInterval);

            AgentUriStatData poll = storage.poll();
            Assert.assertNotNull(poll);

            Collection<EachUriStatData> allUriStatData = poll.getAllUriStatData();
            for (EachUriStatData eachUriStatData : allUriStatData) {
                storeCount -= eachUriStatData.getTotalHistogram().getCount();
            }

            Assert.assertEquals(0, storeCount);
        } finally {
            if (storage != null) {
                storage.close();
            }
        }
    }

    private void storeRandomValue(AsyncQueueingUriStatStorage storage) {
        storage.store(URI_EXAMPLES[RANDOM.nextInt(URI_EXAMPLES.length)], RANDOM.nextBoolean(), RANDOM.nextInt(10000));
    }

}
