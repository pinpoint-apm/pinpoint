/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;


import com.navercorp.pinpoint.bootstrap.config.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ProfilerConfigIT implements ProfilerConfig {
	@Override
	public int getInterceptorRegistrySize() {
		return 0;
	}

	@Override
	public String getCollectorSpanServerIp() {
		return null;
	}

	@Override
	public int getCollectorSpanServerPort() {
		return 0;
	}

	@Override
	public String getCollectorStatServerIp() {
		return null;
	}

	@Override
	public int getCollectorStatServerPort() {
		return 0;
	}

	@Override
	public String getCollectorTcpServerIp() {
		return null;
	}

	@Override
	public int getCollectorTcpServerPort() {
		return 0;
	}

	@Override
	public int getStatDataSenderWriteQueueSize() {
		return 0;
	}

	@Override
	public int getStatDataSenderSocketSendBufferSize() {
		return 0;
	}

	@Override
	public int getStatDataSenderSocketTimeout() {
		return 0;
	}

	@Override
	public String getStatDataSenderSocketType() {
		return null;
	}

	@Override
	public String getStatDataSenderTransportType() {
		return null;
	}

	@Override
	public int getSpanDataSenderWriteQueueSize() {
		return 0;
	}

	@Override
	public int getSpanDataSenderSocketSendBufferSize() {
		return 0;
	}

	@Override
	public boolean isTcpDataSenderCommandAcceptEnable() {
		return true;
	}

	@Override
	public boolean isTcpDataSenderCommandActiveThreadEnable() {
		return true;
	}

	@Override
	public boolean isTcpDataSenderCommandActiveThreadCountEnable() {
		return true;
	}

	@Override
	public boolean isTcpDataSenderCommandActiveThreadDumpEnable() {
		return true;
	}

	@Override
	public boolean isTcpDataSenderCommandActiveThreadLightDumpEnable() {
		return true;
	}

	@Override
	public long getTcpDataSenderPinpointClientWriteTimeout() {
		return 0;
	}

	@Override
	public long getTcpDataSenderPinpointClientRequestTimeout() {
		return 0;
	}

	@Override
	public long getTcpDataSenderPinpointClientReconnectInterval() {
		return 0;
	}

	@Override
	public long getTcpDataSenderPinpointClientPingInterval() {
		return 0;
	}

	@Override
	public long getTcpDataSenderPinpointClientHandshakeInterval() {
		return 0;
	}

	@Override
	public boolean isTraceAgentActiveThread() {
		return true;
	}

	@Override
	public boolean isTraceAgentDataSource() {
		return true;
	}

	@Override
	public int getDataSourceTraceLimitSize() {
		return 0;
	}

	@Override
	public boolean isDeadlockMonitorEnable() {
		return true;
	}

	@Override
	public long getDeadlockMonitorInterval() {
		return 0;
	}

	@Override
	public int getSpanDataSenderSocketTimeout() {
		return 0;
	}

	@Override
	public String getSpanDataSenderSocketType() {
		return null;
	}

	@Override
	public String getSpanDataSenderTransportType() {
		return null;
	}

	@Override
	public int getSpanDataSenderChunkSize() {
		return 0;
	}

	@Override
	public int getStatDataSenderChunkSize() {
		return 0;
	}

	@Override
	public boolean isProfileEnable() {
		return false;
	}

	@Override
	public int getJdbcSqlCacheSize() {
		return 0;
	}

	@Override
	public boolean isTraceSqlBindValue() {
		return false;
	}

	@Override
	public int getMaxSqlBindValueSize() {
		return 0;
	}

	@Override
	public boolean isSamplingEnable() {
		return false;
	}

	@Override
	public int getSamplingRate() {
		return 0;
	}

	@Override
	public boolean isIoBufferingEnable() {
		return false;
	}

	@Override
	public int getIoBufferingBufferSize() {
		return 0;
	}

	@Override
	public String getProfilerJvmVendorName() {
		return null;
	}

	@Override
	public String getProfilerOSName() {
		return null;
	}

	@Override
	public int getProfileJvmStatCollectIntervalMs() {
		return 0;
	}

	@Override
	public int getProfileJvmStatBatchSendCount() {
		return 0;
	}

	@Override
	public boolean isProfilerJvmStatCollectDetailedMetrics() {
		return false;
	}

	@Override
	public long getAgentInfoSendRetryInterval() {
		return 0;
	}

	@Override
	public boolean getStaticResourceCleanup() {
		return false;
	}

	@Override
	public Filter<String> getProfilableClassFilter() {
		return null;
	}

	@Override
	public List<String> getApplicationTypeDetectOrder() {
		return new ArrayList<String>();
	}

	@Override
	public List<String> getDisabledPlugins() {
		return new ArrayList<String>();
	}

	@Override
	public String getApplicationServerType() {
		return "getApplicationServerType";
	}

	@Override
	public int getCallStackMaxDepth() {
		return 0;
	}

	@Override
	public boolean isPropagateInterceptorException() {
		return true;
	}

	@Override
	public String getProfileInstrumentEngine() {
		return null;
	}

	@Override
	public boolean isSupportLambdaExpressions() {
		return true;
	}

	@Override
	public boolean isInstrumentMatcherEnable() {
		return true;
	}

	@Override
	public InstrumentMatcherCacheConfig getInstrumentMatcherCacheConfig() {
		return null;
	}

	@Override
	public boolean isProxyHttpHeaderEnable() {
		return true;
	}

	@Override
	public HttpStatusCodeErrors getHttpStatusCodeErrors() {
		return null;
	}

	@Override
	public String getInjectionModuleFactoryClazzName() {
		return "getInjectionModuleFactoryClazzName";
	}

	@Override
	public String getApplicationNamespace() {
		return "getApplicationNamespace";
	}

	@Override
	public String readString(String propertyName, String defaultValue) {
		return defaultValue;
	}

	@Override
	public int readInt(String propertyName, int defaultValue) {
		return 0;
	}

	@Override
	public DumpType readDumpType(String propertyName, DumpType defaultDump) {
		return null;
	}

	@Override
	public long readLong(String propertyName, long defaultValue) {
		return 0;
	}

	@Override
	public List<String> readList(String propertyName) {
		return new ArrayList<String>();
	}

	@Override
	public boolean readBoolean(String propertyName, boolean defaultValue) {
		return true;
	}

	@Override
	public Map<String, String> readPattern(String propertyNamePatternRegex) {
		return new HashMap<String, String>();
	}
}
