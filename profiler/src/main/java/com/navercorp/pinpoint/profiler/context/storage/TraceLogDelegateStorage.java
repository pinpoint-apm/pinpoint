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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.profiler.context.DefaultTrace;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceLogDelegateStorage implements Storage {

    private final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());

    private final Storage delegate;

    public TraceLogDelegateStorage(Storage delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void store(SpanEvent spanEvent) {
        if (logger.isTraceEnabled()) {
            final Thread th = Thread.currentThread();
            logger.trace("[DefaultTrace] Write {} thread{id={}, name={}}", spanEvent, th.getId(), th.getName());
        }
        this.delegate.store(spanEvent);
    }

    @Override
    public void store(Span span) {
        if (logger.isTraceEnabled()) {
            final Thread th = Thread.currentThread();
            logger.trace("[DefaultTrace] Write {} thread{id={}, name={}}", span, th.getId(), th.getName());
        }
        this.delegate.store(span);
    }

    @Override
    public void flush() {
        this.delegate.flush();
    }

    @Override
    public void close() {
        this.delegate.close();
    }
}
