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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.AgentInfoFactory;

/**
 * @author Woonduk Kang(emeroad)
 * @author HyunGil Jeong
 */
public class AgentInfoSenderProvider implements Provider<AgentInfoSender> {

    private final ProfilerConfig profilerConfig;
    private final Provider<EnhancedDataSender> enhancedDataSenderProvider;
    private final Provider<AgentInfoFactory> agentInfoFactoryProvider;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;

    @Inject
    public AgentInfoSenderProvider(
            ProfilerConfig profilerConfig,
            Provider<EnhancedDataSender> enhancedDataSenderProvider,
            Provider<AgentInfoFactory> agentInfoFactoryProvider,
            ServerMetaDataRegistryService serverMetaDataRegistryService) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.enhancedDataSenderProvider = Assert.requireNonNull(enhancedDataSenderProvider, "enhancedDataSenderProvider must not be null");
        this.agentInfoFactoryProvider = Assert.requireNonNull(agentInfoFactoryProvider, "agentInfoFactoryProvider must not be null");
        this.serverMetaDataRegistryService = Assert.requireNonNull(serverMetaDataRegistryService, "serverMetaDataRegistryService must not be null");
    }

    @Override
    public AgentInfoSender get() {
        final EnhancedDataSender enhancedDataSender = this.enhancedDataSenderProvider.get();
        final AgentInfoFactory agentInfoFactory = this.agentInfoFactoryProvider.get();
        final AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(enhancedDataSender, agentInfoFactory)
                .sendInterval(profilerConfig.getAgentInfoSendRetryInterval())
                .build();
        serverMetaDataRegistryService.addListener(new ServerMetaDataRegistryService.OnChangeListener() {
            @Override
            public void onServerMetaDataChange() {
                agentInfoSender.refresh();
            }
        });
        return agentInfoSender;
    }
}
