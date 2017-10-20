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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncId implements AsyncId {

    private static final AtomicIntegerFieldUpdater<DefaultAsyncId> ASYNC_SEQUENCE_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(DefaultAsyncId.class, "asyncSequence");

    private final int asyncId;

    @SuppressWarnings("unused")
    private volatile int asyncSequence = 0;

    public DefaultAsyncId(int asyncId) {
        this.asyncId = asyncId;
    }

    @Override
    public int getAsyncId() {
        return asyncId;
    }

    @Override
    public short nextAsyncSequence() {
        return (short) ASYNC_SEQUENCE_UPDATER.incrementAndGet(this);
    }

    @Override
    public String toString() {
        return "DefaultAsyncId{" +
                "asyncId=" + asyncId +
                ", asyncSequence=" + asyncSequence +
                '}';
    }
}
