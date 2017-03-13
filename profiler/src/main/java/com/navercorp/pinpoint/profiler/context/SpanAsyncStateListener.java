/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.profiler.context.id.ListenableAsyncState;
import com.navercorp.pinpoint.profiler.context.storage.Storage;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 */
@InterfaceAudience.LimitedPrivate("vert.x")
public class SpanAsyncStateListener implements ListenableAsyncState.AsyncStateListener {

    private final AtomicIntegerFieldUpdater<SpanAsyncStateListener> CLOSED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(SpanAsyncStateListener.class, "closed");
    private static final int OPEN = 0;
    private static final int CLOSED = 1;

    @SuppressWarnings("unused")
    private volatile int closed = OPEN;

    private final Span span;
    private final Storage storage;

    SpanAsyncStateListener(Span span, Storage storage) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        if (storage == null) {
            throw new NullPointerException("storage must not be null");
        }
        this.span = span;
        this.storage = storage;
    }

    @Override
    public void finish() {
        if (CLOSED_UPDATER.compareAndSet(this, OPEN, CLOSED)) {
            if (span.isTimeRecording()) {
                span.markAfterTime();
            }
            this.storage.store(this.span);
            this.storage.close();
        }
    }
}
