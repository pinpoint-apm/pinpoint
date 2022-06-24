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
import com.navercorp.pinpoint.rpc.ResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class RequestManagerTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private Timer timer;
    private RequestManager requestManager;

    @BeforeEach
    public void setUp() throws Exception {
        this.timer = new HashedWheelTimer(10, TimeUnit.MICROSECONDS);
        this.requestManager = new RequestManager(timer, 3000);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (this.timer != null) {
            this.timer.stop();
        }
        if (this.requestManager != null) {
            this.requestManager.close();
        }
    }

    @Test
    public void testRegisterRequest() throws Exception {
        final int requestId = requestManager.nextRequestId();
        final Future<ResponseMessage> future = requestManager.register(requestId, 50);

        Assertions.assertTrue(future.await(200));
        Assertions.assertTrue(future.isReady());
        Assertions.assertFalse(future.isSuccess());
        Assertions.assertTrue(future.getCause().getMessage().contains("timeout"));
        logger.debug(future.getCause().getMessage());
    }


    @Test
    public void testRemoveMessageFuture() throws Exception {
        int requestId = requestManager.nextRequestId();

        DefaultFuture<ResponseMessage> future = requestManager.register(requestId, 2000);
        future.setFailure(new RuntimeException());

        Future<ResponseMessage> nullFuture = requestManager.removeMessageFuture(requestId);
        Assertions.assertNull(nullFuture);
    }

}
