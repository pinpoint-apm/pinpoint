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

import com.google.common.util.concurrent.Uninterruptibles;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.EachUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.URIKey;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.UriStatHistogram;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class AsyncQueueingUriStatStorageTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String[] URI_EXAMPLES = {"/index.html", "/main", "/error"};

    private static final String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE"};

    @Test
    public void storageTest() {
        int collectInterval = 100;
        storageTest(collectInterval, 1);
        storageTest(collectInterval, 2);
        storageTest(collectInterval, 3);
        storageTest(collectInterval, 4);
    }

    private void storageTest(int collectInterval, int storeCount) {
        try (AsyncQueueingUriStatStorage storage
                     = new AsyncQueueingUriStatStorage(true, 5012, 1000, "Test-Executor", collectInterval)) {

            long sleepTime = System.currentTimeMillis() % collectInterval;

            Uninterruptibles.sleepUninterruptibly(sleepTime + 2, TimeUnit.MILLISECONDS);

            for (int i = 0; i < storeCount; i++) {
                final long timestamp = System.currentTimeMillis();
                storeRandomValue(storage, timestamp);
            }

            Assertions.assertNull(storage.poll());

            Uninterruptibles.sleepUninterruptibly(collectInterval, TimeUnit.MILLISECONDS);

            storage.pollTimeout(collectInterval);

            AgentUriStatData poll = storage.poll();
            Assertions.assertNotNull(poll);
            Set<Map.Entry<URIKey, EachUriStatData>> allUriStatData = poll.getAllUriStatData();
            storeCount -= allUriStatData
                    .stream()
                    .map(Map.Entry::getValue)
                    .map(EachUriStatData::getTotalHistogram)
                    .mapToLong(UriStatHistogram::getCount)
                    .sum();

            Assertions.assertEquals(0, storeCount);
        }
    }

    private void storeRandomValue(AsyncQueueingUriStatStorage storage, long timestamp) {
        storage.store(URI_EXAMPLES[RANDOM.nextInt(URI_EXAMPLES.length)],
                HTTP_METHODS[RANDOM.nextInt(HTTP_METHODS.length)],
                RANDOM.nextBoolean(), timestamp - RANDOM.nextInt(10000), timestamp);
    }

}
