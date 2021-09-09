/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.config;

import com.navercorp.pinpoint.bootstrap.config.Value;

public class DefaultContextConfig implements ContextConfig {

    @Value("${profiler.pinpoint.activethread}")
    private boolean traceAgentActiveThread = true;

    @Value("${profiler.pinpoint.datasource}")
    private boolean traceAgentDataSource = false;

    @Value("${profiler.pinpoint.datasource.tracelimitsize}")
    private int dataSourceTraceLimitSize = 20;

    @Value("${profiler.monitor.deadlock.enable}")
    private boolean deadlockMonitorEnable = true;
    @Value("${profiler.monitor.deadlock.interval}")
    private long deadlockMonitorInterval = 60000L;


    @Value("${profiler.sampling.new.throughput}")
    // Throughput sampling
    private int samplingNewThroughput = 0;
    @Value("${profiler.sampling.continue.throughput}")
    private int samplingContinueThroughput = 0;

    // span buffering
    // configuration for sampling and IO buffer
    @Value("${profiler.io.buffering.enable}")
    private boolean ioBufferingEnable = true;
    // it may be a problem to be here.  need to modify(delete or move or .. )  this configuration.
    @Value("${profiler.io.buffering.buffersize}")
    private int ioBufferingBufferSize = 20;

    @Value("profiler.jvm.vendor.name")
    private String profileJvmVendorName;
    // JVM
    @Value("${profiler.os.name}")
    private String profileOsName;

    private final long DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL = 5 * 60 * 1000L;
    @Value("${profiler.agentInfo.send.retry.interval}")
    private long agentInfoSendRetryInterval = DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL;

    // proxy http header names
    @Value("${profiler.proxy.http.header.enable}")
    private boolean proxyHttpHeaderEnable = true;

    public DefaultContextConfig() {
    }

    @Override
    public boolean isTraceAgentActiveThread() {
        return traceAgentActiveThread;
    }

    @Override
    public boolean isTraceAgentDataSource() {
        return traceAgentDataSource;
    }

    @Override
    public int getDataSourceTraceLimitSize() {
        return dataSourceTraceLimitSize;
    }

    @Override
    public boolean isDeadlockMonitorEnable() {
        return deadlockMonitorEnable;
    }

    @Override
    public long getDeadlockMonitorInterval() {
        return deadlockMonitorInterval;
    }

    @Override
    public int getSamplingNewThroughput() {
        return samplingNewThroughput;
    }

    @Override
    public int getSamplingContinueThroughput() {
        return samplingContinueThroughput;
    }

    @Override
    public boolean isIoBufferingEnable() {
        return ioBufferingEnable;
    }

    @Override
    public int getIoBufferingBufferSize() {
        return ioBufferingBufferSize;
    }

    @Override
    public String getProfilerJvmVendorName() {
        return profileJvmVendorName;
    }

    @Override
    public String getProfilerOSName() {
        return profileOsName;
    }

    @Override
    public long getAgentInfoSendRetryInterval() {
        return agentInfoSendRetryInterval;
    }

    @Override
    public boolean isProxyHttpHeaderEnable() {
        return proxyHttpHeaderEnable;
    }

    @Override
    public String toString() {
        return "DefaultContextConfig{" +
                "traceAgentActiveThread=" + traceAgentActiveThread +
                ", traceAgentDataSource=" + traceAgentDataSource +
                ", dataSourceTraceLimitSize=" + dataSourceTraceLimitSize +
                ", deadlockMonitorEnable=" + deadlockMonitorEnable +
                ", deadlockMonitorInterval=" + deadlockMonitorInterval +
                ", samplingNewThroughput=" + samplingNewThroughput +
                ", samplingContinueThroughput=" + samplingContinueThroughput +
                ", ioBufferingEnable=" + ioBufferingEnable +
                ", ioBufferingBufferSize=" + ioBufferingBufferSize +
                ", profileJvmVendorName='" + profileJvmVendorName + '\'' +
                ", profileOsName='" + profileOsName + '\'' +
                ", DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL=" + DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL +
                ", agentInfoSendRetryInterval=" + agentInfoSendRetryInterval +
                ", proxyHttpHeaderEnable=" + proxyHttpHeaderEnable +
                '}';
    }
}
