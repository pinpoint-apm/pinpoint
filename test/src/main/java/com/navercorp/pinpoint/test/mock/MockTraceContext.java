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

package com.navercorp.pinpoint.test.mock;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class MockTraceContext implements TraceContext {

    private Trace trace;
    private final ProfilerConfig config = new DefaultProfilerConfig();

    public void setTrace(Trace trace) {
        this.trace = trace;
    }

    @Override
    public Trace currentTraceObject() {
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return null;
        }
        return trace;
    }

    @Override
    public Trace currentRawTraceObject() {
        return trace;
    }

    @Override
    public Trace continueTraceObject(TraceId traceID) {
        return trace;
    }

    @Override
    public Trace newTraceObject() {
        return trace;
    }

    @Override
    public String getAgentId() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return null;
    }

    @Override
    public long getAgentStartTime() {
        return 0;
    }

    @Override
    public short getServerTypeCode() {
        return 0;
    }

    @Override
    public String getServerType() {
        return null;
    }

    @Override
    public int cacheApi(MethodDescriptor methodDescriptor) {
        return 0;
    }

    @Override
    public int cacheString(String value) {
        return 0;
    }

    @Override
    public ParsingResult parseSql(String sql) {
        return null;
    }

    @Override
    public boolean cacheSql(ParsingResult parsingResult) {
        return false;
    }

    @Override
    public TraceId createTraceId(String transactionId, long parentSpanId, long spanId, short flags) {
        return null;
    }

    @Override
    public Trace disableSampling() {
        return null;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return config;
    }

    @Override
    public ServerMetaDataHolder getServerMetaDataHolder() {
        return null;
    }

    @Override
    public Trace newAsyncTraceObject() {
        return null;
    }

    @Override
    public Trace continueAsyncTraceObject(TraceId traceId) {
        return null;
    }

    @Override
    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
        return null;
    }

    @Override
    public int getAsyncId() {
        return 0;
    }

    @Override
    public Trace continueTraceObject(Trace trace) {
        return null;
    }

    @Override
    public Trace removeTraceObject() {
        return null;
    }

    @Override
    public JdbcContext getJdbcContext() {
//        return new
        return null;
    }

}