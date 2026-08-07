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

import kotlin.coroutines.intrinsics.IntrinsicsKt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class KtorClientContinuationInterceptorTest {

    @Test
    void resumedResultFinishesHolderAndClearsAccessor() {
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        TestAccessor target = new TestAccessor(holder);

        new KtorClientContinuationInterceptor().after(target, new Object[0], new Object(), null);

        verify(holder, times(1)).finishAsync(null);
        assertNull(target.trace);
    }

    @Test
    void suspendedMarkerReturnsSkipsFinishButKeepsHolder() {
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        TestAccessor target = new TestAccessor(holder);
        Object marker = IntrinsicsKt.getCOROUTINE_SUSPENDED();

        new KtorClientContinuationInterceptor().after(target, new Object[0], marker, null);

        verify(holder, never()).finishAsync(null);
        assertSame(holder, target.trace);
    }

    @Test
    void missingHolderIsNoOp() {
        TestAccessor target = new TestAccessor(null);

        new KtorClientContinuationInterceptor().after(target, new Object[0], new Object(), null);

        assertNull(target.trace);
    }

    @Test
    void nonAccessorTargetIsIgnored() {
        new KtorClientContinuationInterceptor().after(new Object(), new Object[0], new Object(), null);
        // no-op expectation
    }

    @Test
    void holderThrowingDoesNotPropagateAndClearsAccessor() {
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        doThrow(new IllegalStateException("finishAsync boom")).when(holder).finishAsync(null);
        TestAccessor target = new TestAccessor(holder);

        new KtorClientContinuationInterceptor().after(target, new Object[0], new Object(), null);

        assertNull(target.trace);
    }

    private static class TestAccessor implements KtorClientTraceAccessor {
        KtorClientTraceHolder trace;

        TestAccessor(KtorClientTraceHolder trace) {
            this.trace = trace;
        }

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
