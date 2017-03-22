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
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentInfoSenderProvider implements Provider<AgentInfoSender> {

    private final ProfilerConfig profilerConfig;
    private final Provider<EnhancedDataSender> enhancedDataSenderProvider;
    private final Provider<AgentInformation> agentInformationProvider;
    private final JvmInformation jvmInformation;

    @Inject
    public AgentInfoSenderProvider(ProfilerConfig profilerConfig, Provider<EnhancedDataSender> enhancedDataSenderProvider, Provider<AgentInformation> agentInformationProvider, JvmInformation jvmInformation) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (enhancedDataSenderProvider == null) {
            throw new NullPointerException("enhancedDataSenderProvider must not be null");
        }
        if (agentInformationProvider == null) {
            throw new NullPointerException("agentInformationProvider must not be null");
        }
        if (jvmInformation == null) {
            throw new NullPointerException("jvmInformation must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.enhancedDataSenderProvider = enhancedDataSenderProvider;
        this.agentInformationProvider = agentInformationProvider;
        this.jvmInformation = jvmInformation;
    }

    @Override
    public AgentInfoSender get() {
        final EnhancedDataSender enhancedDataSender = this.enhancedDataSenderProvider.get();
        final AgentInformation agentInformation = this.agentInformationProvider.get();
        final AgentInfoSender.Builder builder = new AgentInfoSender.Builder(enhancedDataSender, agentInformation, jvmInformation);
        builder.sendInterval(profilerConfig.getAgentInfoSendRetryInterval());
        return builder.build();
    }
}
