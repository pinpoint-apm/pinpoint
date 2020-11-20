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

package com.navercorp.pinpoint.grpc.client.interceptor;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class DiscardLimiterTest {

    @Test
    public void discardCase1() throws Exception {
        DiscardLimiter discardLimiter = new DiscardLimiter(100, 100);
        discardLimiter.reset();

        // Ready
        for (int i = 0; i < 1000; i++) {
            try {
                discardLimiter.discard(true);
            } catch (DiscardLimiter.DiscardLimiterException e) {
                fail("unexpected condition");
            }
        }
    }

    @Test
    public void discardCase2() throws Exception {
        DiscardLimiter discardLimiter = new DiscardLimiter(100, 100);
        discardLimiter.reset();

        // Not ready
        for (int i = 0; i < 100; i++) {
            try {
                discardLimiter.discard(false);
            } catch (DiscardLimiter.DiscardLimiterException e) {
                fail("unexpected condition");
            }
        }

        TimeUnit.MILLISECONDS.sleep(200);
        boolean onException = false;
        try {
            discardLimiter.discard(false);
        } catch (DiscardLimiter.DiscardLimiterException e) {
            onException = true;
        }
        assertTrue(onException);
    }

    @Test
    public void discardCase3() throws Exception {
        DiscardLimiter discardLimiter = new DiscardLimiter(100, 100);
        discardLimiter.reset();

        // Not ready
        try {
            discardLimiter.discard(false);
        } catch (DiscardLimiter.DiscardLimiterException e) {
            fail("unexpected condition");
        }
        // Reach not ready timeout
        TimeUnit.MILLISECONDS.sleep(200);

        for (int i = 0; i < 99; i++) {
            try {
                discardLimiter.discard(false);
            } catch (DiscardLimiter.DiscardLimiterException e) {
                fail("unexpected condition");
            }
        }

        boolean onException = false;
        try {
            discardLimiter.discard(false);
        } catch (DiscardLimiter.DiscardLimiterException e) {
            onException = true;
        }
        assertTrue(onException);
    }

    @Test
    public void checkDiscardCountForReconnect() throws Exception {
        DiscardLimiter discardLimiter = new DiscardLimiter(100, 100);
        discardLimiter.reset();

        for (int i = 0; i < 100; i++) {
            assertFalse(discardLimiter.checkDiscardCountForReconnect());
        }
        // Reach the discard count for reconnect
        assertTrue(discardLimiter.checkDiscardCountForReconnect());

        // Ready
        discardLimiter.reset();

        for (int i = 0; i < 100; i++) {
            assertFalse(discardLimiter.checkDiscardCountForReconnect());
        }
        // Reach the discard count for reconnect
        assertTrue(discardLimiter.checkDiscardCountForReconnect());
    }

    @Test
    public void checkNotReadyTimeout() throws Exception {
        DiscardLimiter discardLimiter = new DiscardLimiter(100, 100);
        discardLimiter.reset();

        for (int i = 0; i < 99; i++) {
            assertFalse(discardLimiter.checkNotReadyTimeout());
        }

        TimeUnit.MILLISECONDS.sleep(200);
        // Reach the discard count for reconnect
        assertTrue(discardLimiter.checkNotReadyTimeout());

        // Ready
        discardLimiter.reset();

        for (int i = 0; i < 99; i++) {
            assertFalse(discardLimiter.checkNotReadyTimeout());
        }

        TimeUnit.MILLISECONDS.sleep(200);
        // Reach the discard count for reconnect
        assertTrue(discardLimiter.checkNotReadyTimeout());
    }
}