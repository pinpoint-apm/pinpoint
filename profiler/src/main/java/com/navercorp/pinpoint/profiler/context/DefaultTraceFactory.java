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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
import com.navercorp.pinpoint.profiler.context.storage.AsyncStorage;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;

/**
 * @author Taejin Koo
 */
public class DefaultTraceFactory implements TraceFactory {

    private final TraceContext traceContext;
    private final StorageFactory storageFactory;

    private volatile boolean activeTraceTracking = false;

    public DefaultTraceFactory(TraceContext traceContext, StorageFactory storageFactory) {
        this.traceContext = traceContext;
        this.storageFactory = storageFactory;
    }

    @Override
    public Trace createDefaultTrace(long transactionId, TraceType traceType, boolean sampling) {
        DefaultTrace trace = new DefaultTrace(traceContext, transactionId, sampling);
        trace.setTraceType(traceType);
        TraceId traceId = trace.getTraceId();
        Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        return trace;
    }

    @Override
    public Trace createDefaultTrace(TraceId continueTraceId, boolean sampling) {
        DefaultTrace trace = new DefaultTrace(traceContext, continueTraceId, sampling);
        Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        return trace;
    }

    @Override
    public Trace createAsyncTrace(AsyncTraceId traceId, int asyncId, long startTime, boolean sampling) {
        final TraceId parentTraceId = traceId.getParentTraceId();
        final DefaultTrace trace = new DefaultTrace(traceContext, parentTraceId, sampling);
        final Storage storage = storageFactory.createStorage();
        trace.setStorage(new AsyncStorage(storage));

        AsyncTrace asyncTrace = new AsyncTrace(trace, asyncId, traceId.nextAsyncSequence(), startTime);
        return asyncTrace;
    }

    @Override
    public Trace createMetricTrace() {
        return DisableTrace.INSTANCE;
    }

}
