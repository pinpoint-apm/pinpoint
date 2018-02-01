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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.profiler.monitor.collector.businesslog.BusinessLogV1Collector;
import com.navercorp.pinpoint.profiler.monitor.collector.businesslog.BusinessLogVXMetaCollector;

public class BusinessLogVXMetaCollectorProvider implements Provider<BusinessLogV1Collector>{
	
	private ProfilerConfig profilerConfig;

	private String agentId;

	private String jarPath;
	
	@Inject
	public BusinessLogVXMetaCollectorProvider(ProfilerConfig profilerConfig, @AgentId String agentId) {
		this.profilerConfig = profilerConfig;
		this.agentId = agentId;
	}

	@Override
	public BusinessLogV1Collector get() {
		// TODO Auto-generated method stub
		BusinessLogV1Collector businessLogV1Collector = new BusinessLogV1Collector(profilerConfig, agentId);
		return businessLogV1Collector;
	}

}
