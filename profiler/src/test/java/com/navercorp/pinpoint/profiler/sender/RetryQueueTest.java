/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class RetryQueueTest {
    @Test
    public void size() {
        RetryQueue retryQueue = new RetryQueue(1, 1);
        retryQueue.add(new RetryMessage(1, new byte[0]));
        retryQueue.add(new RetryMessage(1, new byte[0]));

        Assert.assertEquals(1, retryQueue.size());
    }

    @Test
    public void size2() {

        RetryQueue retryQueue = new RetryQueue(1, 1);
        RetryMessage retryMessage = retryQueue.get();
        Assert.assertNull(retryMessage);
    }

    @Test
    public void maxRetryTest() {
        RetryQueue retryQueue = new RetryQueue(3, 2);
        RetryMessage retryMessage = new RetryMessage(0, new byte[0]);
        retryMessage.fail();
        retryMessage.fail();


        retryQueue.add(retryMessage);
        retryQueue.add(retryMessage);

        Assert.assertEquals(retryQueue.size(), 0);
    }

    @Test
    public void maxRetryTest2() {
        RetryQueue retryQueue = new RetryQueue(3, 1);
        RetryMessage retryMessage = new RetryMessage(5, new byte[0]);
        retryMessage.fail();
        retryMessage.fail();


        retryQueue.add(retryMessage);
        retryQueue.add(retryMessage);

        Assert.assertEquals(retryQueue.size(), 0);
    }

    @Test
    public void add() {
        RetryQueue retryQueue = new RetryQueue(3, 2);
        retryQueue.add(new RetryMessage(1, new byte[0]));
        // If we add a failed message and it makes the queue filled more than half, the queue must discard it.
        RetryMessage retryMessage = new RetryMessage(1, new byte[0]);
        retryMessage.fail();
        retryQueue.add(retryMessage);

        Assert.assertEquals(retryQueue.size(), 1);
    }
}
