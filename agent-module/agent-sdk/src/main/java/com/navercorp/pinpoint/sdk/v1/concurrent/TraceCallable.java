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
import java.util.concurrent.Callable;

/**
 * {@link Callable} for TraceContext propagation
 * @param <V> return type
 */
public class TraceCallable<V> implements Callable<V> {

    public static <V> Callable<V> wrap(Callable<V> delegate) {
        return new TraceCallable<>(delegate);
    }

    public static <V> Callable<V> asyncEntry(Callable<V> delegate) {
        return new TraceCallable<>(delegate);
    }

    protected final Callable<V> delegate;

    public TraceCallable(Callable<V> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * the starting point of the async execution
     */
    @Override
    public V call() throws Exception {
        return delegate.call();
    }
}
