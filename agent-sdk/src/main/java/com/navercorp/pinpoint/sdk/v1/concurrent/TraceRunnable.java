/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.sdk.v1.concurrent;

import java.util.Objects;

/**
 * {@link Runnable} for TraceContext propagation
 */
public class TraceRunnable implements Runnable {

    public static Runnable wrap(Runnable delegate) {
        return new TraceRunnable(delegate);
    }

    public static Runnable asyncEntry(Runnable delegate) {
        return new TraceRunnable(delegate);
    }

    protected final Runnable delegate;

    public TraceRunnable(Runnable runnable) {
        this.delegate = Objects.requireNonNull(runnable, "delegate");
    }

    /**
     * the starting point of the async execution
     */
    public void run() {
        this.delegate.run();
    }
}
