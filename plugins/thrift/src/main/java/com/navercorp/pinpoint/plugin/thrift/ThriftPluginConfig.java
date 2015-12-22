/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author HyunGil Jeong
 */
public class ThriftPluginConfig {
    private final boolean traceThriftClient;
    private final boolean traceThriftClientAsync;
    private final boolean traceThriftProcessor;
    private final boolean traceThriftProcessorAsync;
    private final boolean traceThriftServiceArgs;
    private final boolean traceThriftServiceResult;
    
    public ThriftPluginConfig(ProfilerConfig src) {
        this.traceThriftClient = src.readBoolean("profiler.thrift.client", true);
        this.traceThriftClientAsync = src.readBoolean("profiler.thrift.client.async", true);
        this.traceThriftProcessor = src.readBoolean("profiler.thrift.processor", true);
        this.traceThriftProcessorAsync = src.readBoolean("profiler.thrift.processor.async", true);
        this.traceThriftServiceArgs = src.readBoolean("profiler.thrift.service.args", false);
        this.traceThriftServiceResult = src.readBoolean("profiler.thrift.service.result", false);
    }
    
    public boolean traceThriftClient() {
        return this.traceThriftClient;
    }

    public boolean traceThriftClientAsync() {
        return this.traceThriftClientAsync;
    }
    
    public boolean traceThriftProcessor() {
        return this.traceThriftProcessor;
    }

    public boolean traceThriftProcessorAsync() {
        return this.traceThriftProcessorAsync;
    }
    
    public boolean traceThriftServiceArgs() {
        return this.traceThriftServiceArgs;
    }
    
    public boolean traceThriftServiceResult() {
        return this.traceThriftServiceResult;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ThriftPluginConfig={traceThriftClient=").append(this.traceThriftClient);
        sb.append(", traceThriftClientAsync=").append(this.traceThriftClientAsync);
        sb.append(", traceThriftProcessor=").append(this.traceThriftProcessor);
        sb.append(", traceThriftProcessorAsync=").append(this.traceThriftProcessorAsync);
        sb.append(", traceThriftServiceArgs=").append(this.traceThriftServiceArgs);
        sb.append(", traceThriftServiceResult=").append(this.traceThriftServiceResult);
        sb.append("}");
        return sb.toString();
    }

}
