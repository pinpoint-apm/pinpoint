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

package com.navercorp.pinpoint.bootstrap.context;

/**
 * A {@link TraceBlock} opened under an {@link AsyncContext}, obtained through
 * {@link StackOperation#getTraceBlock(AsyncContext)}.
 * <p>
 * The {@code AsyncContextTraceBlock*} interceptor bases return one from {@code before()} so that
 * {@code after()} finishes the async trace (its hooks, the async scope and the context's thread
 * binding) with the context {@code before()} used, without looking the context up again on the
 * target. It is a carrier for those bases: interceptor hooks keep using the {@code asyncContext}
 * they receive as an argument.
 */
public interface AsyncContextTraceBlock extends TraceBlock {

    /**
     * @return the AsyncContext this block was opened with; never {@code null}
     */
    AsyncContext getAsyncContext();
}
