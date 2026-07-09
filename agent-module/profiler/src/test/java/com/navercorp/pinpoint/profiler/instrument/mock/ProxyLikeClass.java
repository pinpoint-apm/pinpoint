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
package com.navercorp.pinpoint.profiler.instrument.mock;

import com.navercorp.pinpoint.profiler.instrument.mock.accessor.IntAccessor;

/**
 * Simulates a CGLIB proxy class that copied the accessor members injected into an instrumented class.
 * The accessor interface and getter/setter methods are already declared before instrumentation.
 */
public class ProxyLikeClass implements IntAccessor {
    private int traceInt;

    @Override
    public void _$PINPOINT$_setTraceInt(int value) {
        this.traceInt = value;
    }

    @Override
    public int _$PINPOINT$_getTraceInt() {
        return this.traceInt;
    }
}
