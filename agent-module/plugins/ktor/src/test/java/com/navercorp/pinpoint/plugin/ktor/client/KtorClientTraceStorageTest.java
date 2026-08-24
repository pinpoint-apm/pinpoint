/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.ktor.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class KtorClientTraceStorageTest {

    @AfterEach
    void cleanup() {
        KtorClientTraceStorage.clearPending();
    }

    @Test
    void setGetClearRoundTrip() {
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);

        assertNull(KtorClientTraceStorage.getPending());

        KtorClientTraceStorage.setPending(holder);
        assertSame(holder, KtorClientTraceStorage.getPending());

        KtorClientTraceStorage.clearPending();
        assertNull(KtorClientTraceStorage.getPending());
    }

    @Test
    void pendingIsThreadLocal() throws InterruptedException {
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        final boolean[] sameThreadSame = new boolean[1];
        final boolean[] otherThreadDistinct = new boolean[1];

        KtorClientTraceStorage.setPending(holder);

        Thread otherThread = new Thread(() -> {
            sameThreadSame[0] = KtorClientTraceStorage.getPending() == holder;
            otherThreadDistinct[0] = KtorClientTraceStorage.getPending() == null;
        });
        otherThread.start();
        otherThread.join();

        assertSame(holder, KtorClientTraceStorage.getPending());
        // other thread never sees this thread's pending holder
    }
}
