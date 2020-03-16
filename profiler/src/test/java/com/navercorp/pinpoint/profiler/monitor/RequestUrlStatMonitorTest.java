/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractor;
import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorType;
import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorTypeFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author Taejin Koo
 */
public class RequestUrlStatMonitorTest {

    private final long TEST_FLUSH_INTERVAL_MILLIS = 5000;
    private final Random random = new Random(System.currentTimeMillis());

    @Test
    public void immediatelyFlushTest() throws InterruptedException {

        CountDownLatch sendLatch = new CountDownLatch(1);
        MockDataSender mockDataSender = new MockDataSender(sendLatch);

        DefaultRequestUrlStatMonitor<String> requestUrlStatInfoDefaultRequestUrlStatMonitor = null;
        try {
            long startTime = System.currentTimeMillis();
            requestUrlStatInfoDefaultRequestUrlStatMonitor = new DefaultRequestUrlStatMonitor<String>(mockDataSender, new MockUrlMappingExtractor(), TEST_FLUSH_INTERVAL_MILLIS);

            for (int i = 0; i < 5000; i++) {
                requestUrlStatInfoDefaultRequestUrlStatMonitor.store("test", "test", 200, startTime, startTime + random.nextInt(3000));
            }

            sendLatch.await();

            Assert.assertTrue(System.currentTimeMillis() - startTime < TEST_FLUSH_INTERVAL_MILLIS);
        } finally {
            if (requestUrlStatInfoDefaultRequestUrlStatMonitor != null) {
                requestUrlStatInfoDefaultRequestUrlStatMonitor.close();
            }
        }
    }

    @Test
    public void timeoutFlushTest() throws InterruptedException {
        CountDownLatch sendLatch = new CountDownLatch(1);
        MockDataSender mockDataSender = new MockDataSender(sendLatch);

        DefaultRequestUrlStatMonitor<String> requestUrlStatInfoDefaultRequestUrlStatMonitor = null;
        try {
            long startTime = System.currentTimeMillis();
            requestUrlStatInfoDefaultRequestUrlStatMonitor = new DefaultRequestUrlStatMonitor<String>(mockDataSender, new MockUrlMappingExtractor(), TEST_FLUSH_INTERVAL_MILLIS);

            for (int i = 0; i < 10; i++) {
                requestUrlStatInfoDefaultRequestUrlStatMonitor.store("test", "test", 200, startTime, startTime + random.nextInt(3000));
            }

            sendLatch.await();

            Assert.assertFalse(System.currentTimeMillis() - startTime < TEST_FLUSH_INTERVAL_MILLIS - 500);
        } finally {
            if (requestUrlStatInfoDefaultRequestUrlStatMonitor != null) {
                requestUrlStatInfoDefaultRequestUrlStatMonitor.close();
            }
        }
    }


    private static class MockDataSender implements DataSender {

        private final CountDownLatch countDownLatch;

        public MockDataSender(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public boolean send(Object data) {
            countDownLatch.countDown();
            return true;
        }

        @Override
        public void stop() {
        }

    }

    private static class MockUrlMappingExtractor implements RequestUrlMappingExtractor {

        @Override
        public RequestUrlMappingExtractorType getType() {
            RequestUrlMappingExtractorType mock = RequestUrlMappingExtractorTypeFactory.of("Mock", Object.class);
            return mock;
        }

        @Override
        public String getUrlMapping(Object target, String rawUrl) {
            return rawUrl;
        }

    }

}

