/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.common.util.concurrent;

import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author Taejin Koo
 */
public class OrderedThreadPoolExecutorTest {

    private static final ConcurrentHashMap<String, List<Integer>> testMap = new ConcurrentHashMap();

    @Test
    public void executorOrderTest() throws Exception {
        int testCount = 20;

        CountDownLatch latch = new CountDownLatch(testCount * 2);
        OrderedThreadPoolExecutor orderedThreadPoolExecutor = new OrderedThreadPoolExecutor(String.class, 4, 16);

        try {
            for (int i = 0; i < testCount; i++) {
                orderedThreadPoolExecutor.execute(new OrderedExecuteTestRunnable("1", i, latch));
            }

            for (int i = 0; i < testCount; i++) {
                orderedThreadPoolExecutor.execute(new OrderedExecuteTestRunnable("2", i, latch));
            }

            boolean await = latch.await(10000, TimeUnit.MILLISECONDS);
            if (!await) {
                Assert.fail();
            }

            Set<Map.Entry<String, List<Integer>>> entries = testMap.entrySet();

            for (Map.Entry<String, List<Integer>> entry : testMap.entrySet()) {
                List<Integer> orderList = entry.getValue();

                Integer beforeOrder = null;
                for (Integer order : orderList) {
                    if (beforeOrder != null) {
                        if (order <= beforeOrder) {
                            Assert.fail();
                        }
                    }
                    beforeOrder = order;
                }
            }


        } finally {
            orderedThreadPoolExecutor.shutdown();
            orderedThreadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS);
        }
    }

    @Ignore
    @Test
    public void threadTest() throws Exception {
        int corePoolSize = 4;
        int maximumPoolSize = 16;
        long keepAliveTime = 3000;

        OrderedThreadPoolExecutor orderedThreadPoolExecutor = new OrderedThreadPoolExecutor(String.class, corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS);
        int threadSize = getThreadSize("OrderedThreadPoolExecutor");
        Assert.assertEquals(corePoolSize, threadSize);
        try {

            int executeCount = maximumPoolSize + 100;
            CountDownLatch latch = new CountDownLatch(executeCount);

            for (int i = 0; i < maximumPoolSize; i++) {
                orderedThreadPoolExecutor.execute(new OrderedExecuteTestRunnable("" + i, 0, latch, 5000L));
            }

            threadSize = getThreadSize("OrderedThreadPoolExecutor");
            Assert.assertEquals(maximumPoolSize, threadSize);

            for (int i = maximumPoolSize; i < maximumPoolSize + 100; i++) {
                orderedThreadPoolExecutor.execute(new OrderedExecuteTestRunnable("" + i, 0, latch));
            }

            threadSize = getThreadSize("OrderedThreadPoolExecutor");
            Assert.assertEquals(maximumPoolSize, threadSize);

            boolean await = latch.await(10000, TimeUnit.MILLISECONDS);
            if (!await) {
                Assert.fail();
            }

            Thread.sleep(keepAliveTime + 1000);
            threadSize = getThreadSize("OrderedThreadPoolExecutor");
            Assert.assertEquals(corePoolSize, threadSize);
        } finally {
            orderedThreadPoolExecutor.shutdown();
            orderedThreadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS);
        }

        threadSize = getThreadSize("OrderedThreadPoolExecutor");
        Assert.assertEquals(0, threadSize);


    }

    private int getThreadSize(String threadPrefixName) {
        int matchedThreadSize = 0;

        ThreadInfo[] threadInfos = ThreadMXBeanUtils.dumpAllThread();
        for (ThreadInfo info : threadInfos) {
            if (info.getThreadName().startsWith(threadPrefixName)) {
                matchedThreadSize++;
            }
        }

        return matchedThreadSize;
    }

    class OrderedExecuteTestRunnable implements OrderedThreadPoolRunnable<String> {

        private final String key;
        private final int order;
        private final CountDownLatch latch;
        private final Long sleepTime;

        public OrderedExecuteTestRunnable(String key, int order, CountDownLatch latch) {
            this(key, order, latch, 0L);
        }

        public OrderedExecuteTestRunnable(String key, int order, CountDownLatch latch, Long sleepTime) {
            this.key = key;
            this.order = order;
            this.latch = latch;
            this.sleepTime = sleepTime;
        }

        @Override
        public String getOrderKey() {
            return key;
        }

        @Override
        public void run() {
            try {
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                List<Integer> executeOrder = testMap.get(key);
                if (executeOrder == null) {
                    executeOrder = new ArrayList();
                    testMap.put(key, executeOrder);
                }
                executeOrder.add(order);
            } finally {
                latch.countDown();
            }
        }
    }

}
