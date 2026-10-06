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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextTraceBlock;
import com.navercorp.pinpoint.bootstrap.context.Trace;

import java.util.Objects;

/**
 * A {@link DefaultTraceBlock} that also carries the {@link AsyncContext} it was opened under.
 */
public class DefaultAsyncContextTraceBlock extends DefaultTraceBlock implements AsyncContextTraceBlock {

    private final AsyncContext asyncContext;

    public DefaultAsyncContextTraceBlock(Trace trace, AsyncContext asyncContext) {
        super(trace);
        this.asyncContext = Objects.requireNonNull(asyncContext, "asyncContext");
    }

    @Override
    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    @Override
    public String toString() {
        return "DefaultAsyncContextTraceBlock{" +
                "trace=" + getTrace() +
                ", begin=" + isBegin() +
                ", asyncContext=" + asyncContext +
                '}';
    }
}
