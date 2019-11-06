/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
public class PinpointWebSocketTimerTaskDecoratorTest {

    private static final long DELAY_MS = 1000L;

    private final TimerTaskDecoratorFactory timerTaskDecoratorFactory = new PinpointWebSocketTimerTaskDecoratorFactory();

    @Test
    public void testAuthenticationPropagation() throws InterruptedException {
        final int numThreads = 3;
        final Authentication[] authentications = new Authentication[numThreads];
        for (int i = 0; i < authentications.length; i++) {
            final String principal = "principal" + i;
            final String credential = "credential" + i;
            authentications[i] = new TestingAuthenticationToken(principal, credential);
        }
        final CountDownLatch schedulerLatch = new CountDownLatch(numThreads);
        final Timer timer = new Timer();

        for (Authentication authentication : authentications) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SecurityContext securityContext = new SecurityContextImpl();
                    securityContext.setAuthentication(authentication);
                    SecurityContextHolder.setContext(securityContext);
                    TimerTask timerTask = timerTaskDecoratorFactory.createTimerTaskDecorator().decorate(new TestTimerTask(schedulerLatch, authentication));
                    timer.schedule(timerTask, DELAY_MS);
                }
            }).start();
        }
        Assert.assertTrue("Timed out waiting for timer task completion", schedulerLatch.await(2 * DELAY_MS, TimeUnit.MILLISECONDS));
    }

    private static class TestTimerTask extends TimerTask {

        private final CountDownLatch executeLatch;
        private final Authentication expectedAuthentication;

        private TestTimerTask(CountDownLatch executeLatch, Authentication expectedAuthentication) {
            this.executeLatch = Objects.requireNonNull(executeLatch, "executeLatch");
            this.expectedAuthentication = Objects.requireNonNull(expectedAuthentication, "expectedAuthentication");
        }

        @Override
        public void run() {
            try {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                Assert.assertNotNull(securityContext);
                Authentication actualAuthentication = securityContext.getAuthentication();
                Assert.assertSame(expectedAuthentication, actualAuthentication);
            } finally {
                executeLatch.countDown();
            }
        }
    }
}
