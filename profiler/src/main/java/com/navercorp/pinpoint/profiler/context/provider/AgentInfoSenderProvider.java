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
import com.google.inject.Singleton;
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
    private final EnhancedDataSender enhancedDataSender;
    private final AgentInformation agentInformation;
    private final JvmInformation jvmInformation;

    @Inject
    public AgentInfoSenderProvider(ProfilerConfig profilerConfig, EnhancedDataSender enhancedDataSender, AgentInformation agentInformation, JvmInformation jvmInformation) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (enhancedDataSender == null) {
            throw new NullPointerException("enhancedDataSender must not be null");
        }
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        if (jvmInformation == null) {
            throw new NullPointerException("jvmInformation must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.enhancedDataSender = enhancedDataSender;
        this.agentInformation = agentInformation;
        this.jvmInformation = jvmInformation;
    }

    @Override
    public AgentInfoSender get() {
        final AgentInfoSender.Builder builder = new AgentInfoSender.Builder(this.enhancedDataSender, this.agentInformation, jvmInformation);
        builder.sendInterval(profilerConfig.getAgentInfoSendRetryInterval());
        return builder.build();
    }
}
