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

package com.navercorp.pinpoint.bootstrap.interceptor;


import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.util.Clock;
import com.navercorp.pinpoint.common.util.SystemClock;

/**
 * @author emeroad
 */
public class MockTrace implements Trace {

    private long beforeTime;
    private long afterTime;

    private boolean sampled = true;

    private Clock clock = SystemClock.INSTANCE;

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public Thread getBindThread() {
        return null;
    }

    @Override
    public TraceId getTraceId() {
        return null;
    }

    public void setSampled(boolean sampled) {
        this.sampled = sampled;
    }

    @Override
    public boolean canSampled() {
        return sampled;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        return null;
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        return null;
    }

    @Override
    public void traceBlockEnd() {

    }

    @Override
    public void traceBlockEnd(int stackId) {

    }
    
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isRootStack() {
        return false;
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public SpanRecorder getSpanRecorder() {
        return null;
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        return null;
    }

    @Override
    public int getCallStackFrameId() {
        return 0;
    }

//    @Override
//    public void recordLogging(boolean isLogging) {
//    }

    @Override
    public TraceScope getScope(String name) {
        return null;
    }

    @Override
    public TraceScope addScope(String name) {
        return null;
    }
}
