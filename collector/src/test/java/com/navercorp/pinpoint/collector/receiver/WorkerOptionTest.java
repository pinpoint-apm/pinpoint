/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class WorkerOptionTest {

    public static final boolean DEFAULT_COLLECT_METRIC_ENABLE = false;

    @Test
    public void getTest1() throws Exception {
        int workerThreadSize = 1;
        int workerThreadQueueSize = 10;

        DispatchWorkerOption workerOption = new DispatchWorkerOption("testWorker", workerThreadSize, workerThreadQueueSize);

        Assert.assertEquals("testWorker", workerOption.getName());

        Assert.assertEquals(workerThreadSize, workerOption.getThreadSize());
        Assert.assertEquals(workerThreadQueueSize, workerOption.getQueueSize());

        Assert.assertEquals(DEFAULT_COLLECT_METRIC_ENABLE, workerOption.isEnableCollectMetric());
    }

    @Test
    public void getTest2() throws Exception {
        int workerThreadSize = 1;
        int workerThreadQueueSize = 10;
        int recordLogRate = 100;
        boolean collectMetric = true;

        DispatchWorkerOption workerOption = new DispatchWorkerOption("testWorker", workerThreadSize, workerThreadQueueSize, recordLogRate, collectMetric);

        Assert.assertEquals(workerThreadSize, workerOption.getThreadSize());
        Assert.assertEquals(workerThreadQueueSize, workerOption.getQueueSize());
        Assert.assertEquals(recordLogRate, workerOption.getRecordLogRate());

        Assert.assertEquals(collectMetric, workerOption.isEnableCollectMetric());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionTest1() {
        new DispatchWorkerOption("testWorker", 0, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionTest2() {
        new DispatchWorkerOption ("testWorker", 100, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionTest3() {
        new DispatchWorkerOption ("testWorker", 1, 1, 0);
    }

}
