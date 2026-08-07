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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class KtorClientContinuationConstructorInterceptorTest {

    private KtorClientTraceHolder holder;

    @BeforeEach
    void setUp() {
        holder = mock(KtorClientTraceHolder.class);
        KtorClientTraceStorage.clearPending();
    }

    @Test
    void attachesPendingHolderToContinuationTarget() {
        KtorClientTraceStorage.setPending(holder);
        TestContinuationAccessor continuation = new TestContinuationAccessor();

        new KtorClientContinuationConstructorInterceptor().after(continuation, new Object[0], null, null);

        assertSame(holder, continuation.trace);
        // markAttached is invoked against the immutable holder instance
    }

    @Test
    void skipsWhenHolderMissing() {
        TestContinuationAccessor continuation = new TestContinuationAccessor();

        new KtorClientContinuationConstructorInterceptor().after(continuation, new Object[0], null, null);

        assertNull(continuation.trace);
    }

    @Test
    void skipsWhenConstructorThrew() {
        KtorClientTraceStorage.setPending(holder);
        TestContinuationAccessor continuation = new TestContinuationAccessor();

        new KtorClientContinuationConstructorInterceptor()
                .after(continuation, new Object[0], null, new RuntimeException("ctor failure"));

        // even with throwable, the accessor should not receive the holder
        assertNull(continuation.trace);
    }

    @Test
    void skipsWhenTargetNotTraceAccessor() {
        KtorClientTraceStorage.setPending(holder);
        Object accessorTarget = new Object();

        new KtorClientContinuationConstructorInterceptor()
                .after(accessorTarget, new Object[0], null, null);

        // nothing to assert on target; just verify we didn't leak a pending holder on the storage side
        org.junit.jupiter.api.Assertions.assertSame(holder, KtorClientTraceStorage.getPending());
    }

    private static class TestContinuationAccessor implements KtorClientTraceAccessor {
        KtorClientTraceHolder trace;

        @Override
        public void _$PINPOINT$_setKtorClientTrace(KtorClientTraceHolder holder) {
            this.trace = holder;
        }

        @Override
        public KtorClientTraceHolder _$PINPOINT$_getKtorClientTrace() {
            return trace;
        }
    }
}
