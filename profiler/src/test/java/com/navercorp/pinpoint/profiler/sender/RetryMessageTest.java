/*
 * Copyright 2016 NAVER Corp.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class RetryMessageTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void availableTest1() throws Exception {
        RetryMessage retryMessage = new RetryMessage(1, new byte[0]);
        Assert.assertTrue(retryMessage.isRetryAvailable());

        retryMessage.fail();
        Assert.assertFalse(retryMessage.isRetryAvailable());
    }

    @Test
    public void availableTest2() throws Exception {
        RetryMessage retryMessage = new RetryMessage(1, 2, new byte[0]);
        Assert.assertTrue(retryMessage.isRetryAvailable());

        retryMessage.fail();
        Assert.assertFalse(retryMessage.isRetryAvailable());
    }

    @Test
    public void availableTest3() throws Exception {
        RetryMessage retryMessage = new RetryMessage(2, 2, new byte[0]);
        Assert.assertFalse(retryMessage.isRetryAvailable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentTest1() {
        RetryMessage retryMessage = new RetryMessage(-1, new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentTest2() {
        RetryMessage retryMessage = new RetryMessage(-1, 5, new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentTest3() {
        RetryMessage retryMessage = new RetryMessage(10, 9, new byte[0]);
    }

    @Test
    public void test_toSting() {
        RetryMessage message1 = new RetryMessage(1, new byte[0]);
        logger.debug("{}", message1.toString());

        // check null safety
        RetryMessage message2 = new RetryMessage(1, null);
        logger.debug("{}", message2.toString());

        RetryMessage message3 = new RetryMessage(1, null, null);
        logger.debug("{}", message3.toString());
    }


}
