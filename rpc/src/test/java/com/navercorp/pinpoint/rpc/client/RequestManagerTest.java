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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class RequestManagerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Timer timer = getTimer();

    @Before
    public void setUp() throws Exception {
        this.timer = getTimer();

    }

    @After
    public void tearDown() throws Exception {
        if (this.timer != null) {
            this.timer.stop();
        }
    }

    @Test
    public void testRegisterRequest() throws Exception {
        RequestManager requestManager = new RequestManager(timer, 3000);
        try {
            final int requestId = requestManager.nextRequestId();
            final Future future = requestManager.register(requestId, 50);

            TestAwaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return future.isReady();
                }
            }, 10, 200);

            Assert.assertTrue(future.isReady());
            Assert.assertFalse(future.isSuccess());
            Assert.assertTrue(future.getCause().getMessage().contains("timeout"));
            logger.debug(future.getCause().getMessage());
        } finally {
            requestManager.close();
        }
    }


    @Test
    public void testRemoveMessageFuture() throws Exception {
        RequestManager requestManager = new RequestManager(timer, 3000);
        try {
            int requestId = requestManager.nextRequestId();

            DefaultFuture future = requestManager.register(requestId, 2000);
            future.setFailure(new RuntimeException());

            Future nullFuture = requestManager.removeMessageFuture(requestId);
            Assert.assertNull(nullFuture);
        } finally {
            requestManager.close();
        }

    }

    private HashedWheelTimer getTimer() {
        return new HashedWheelTimer(10, TimeUnit.MICROSECONDS);
    }



    @Test
    public void testClose() throws Exception {

    }
}
