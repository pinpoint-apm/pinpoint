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

package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.annotations.InterfaceStability;

/**
 * @since 1.7.0
 * @author Woonduk Kang(emeroad)
 */
@InterfaceStability.Evolving
public interface AsyncContext {

    String ASYNC_TRACE_SCOPE = "##ASYNC_TRACE_SCOPE";

    Trace continueAsyncTraceObject();
    Trace continueAsyncTraceObject(boolean asyncTraceBlock);

    /**
     * Like {@link #continueAsyncTraceObject(boolean)} with {@code false}, but when no trace is
     * bound to the current thread the given previously-created {@code reuse} trace is rebound
     * instead of creating a new one. This lets a caller that receives many signals for one
     * logical async operation (e.g. a reactive subscription) pay the trace creation cost once
     * and rebind per signal, instead of a create/close cycle per signal.
     * <p>
     * If a trace is already bound to the current thread, it is returned (nested) just like
     * {@link #continueAsyncTraceObject(boolean)}. The caller remains responsible for unbinding
     * with {@link #close()} when its outermost activation ends, and for eventually closing the
     * reused trace itself.
     * <p>
     * The default implementation ignores {@code reuse} and creates a new trace, so
     * implementations without rebinding support degrade to the per-signal behavior.
     */
    default Trace continueAsyncTraceObject(Trace reuse) {
        return continueAsyncTraceObject(false);
    }

    Trace currentAsyncTraceObject();

    void close();

    boolean finish();

//    void setAttribute(String name, Object o);
//
//    Object getAttribute(String name);
}
