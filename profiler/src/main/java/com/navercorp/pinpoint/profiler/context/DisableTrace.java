/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.RootCallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;


/**
 * @author emeroad
 * @author jaehong.kim
 */
public class DisableTrace  implements Trace {

    public static final DisableTrace INSTANCE = new DisableTrace();
    
    private DisableTrace() {
    }

    @Override
    public CallStackFrame traceBlockBegin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallStackFrame traceBlockBegin(int stackId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockEnd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TraceId getTraceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSampled() {
        // always return false
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isRootStack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    @Override
    public int getCallStackFrameId() {
        return 0;
    }

    @Override
    public RootCallStackFrame rootCallStackFrame() {
        return null;
    }

    @Override
    public CallStackFrame currentCallStackFrame() {
        return null;
    }

    @Override
    public TraceType getTraceType() {
        return TraceType.DISABLE;
    }
}