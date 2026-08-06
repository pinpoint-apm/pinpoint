/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.reactorsupport;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Test double faithfully simulating DefaultAsyncContext's single-threaded binding contract:
 * creating or rebinding a trace binds it to the (one) simulated thread, {@link #close()} unbinds,
 * and every context sharing the {@code boundSlot} sees the same binding — exactly what nested
 * windows and control windows observe for real. Counts are emergent, not choreographed.
 */
final class StubAsyncContext implements AsyncContext {
    private final Trace[] boundSlot;
    private final Deque<Trace> tracesToCreate;
    int creations;
    int unbinds;

    StubAsyncContext(Trace[] boundSlot, Trace... tracesToCreate) {
        this.boundSlot = boundSlot;
        this.tracesToCreate = new ArrayDeque<>(Arrays.asList(tracesToCreate));
    }

    @Override
    public Trace continueAsyncTraceObject() {
        return continueAsyncTraceObject(false);
    }

    @Override
    public Trace continueAsyncTraceObject(boolean asyncTraceBlock) {
        final Trace created = tracesToCreate.poll();
        if (created == null) {
            return null;
        }
        creations++;
        boundSlot[0] = created;
        return created;
    }

    @Override
    public Trace continueAsyncTraceObject(Trace reuse) {
        boundSlot[0] = reuse;
        return reuse;
    }

    @Override
    public Trace currentAsyncTraceObject() {
        return boundSlot[0];
    }

    @Override
    public void close() {
        unbinds++;
        boundSlot[0] = null;
    }

    @Override
    public boolean finish() {
        return false;
    }
}
