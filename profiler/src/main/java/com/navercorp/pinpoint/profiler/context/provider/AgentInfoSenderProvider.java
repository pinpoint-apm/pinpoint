/*
 * Copyright 2018 NAVER Corp.
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
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.config.ContextConfig;
import com.navercorp.pinpoint.profiler.context.module.AgentDataSender;
import com.navercorp.pinpoint.profiler.context.module.ResultConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.ResultResponse;
import com.navercorp.pinpoint.profiler.util.AgentInfoFactory;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author HyunGil Jeong
 */
public class AgentInfoSenderProvider implements Provider<AgentInfoSender> {

    private final ContextConfig contextConfig;
    private final Provider<EnhancedDataSender<MetaDataType>> enhancedDataSenderProvider;
    private final Provider<AgentInfoFactory> agentInfoFactoryProvider;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;
    private final MessageConverter<Object, ResultResponse> messageConverter;

    @Inject
    public AgentInfoSenderProvider(
            ContextConfig contextConfig,
            @AgentDataSender Provider<EnhancedDataSender<MetaDataType>> enhancedDataSenderProvider,
            Provider<AgentInfoFactory> agentInfoFactoryProvider,
            ServerMetaDataRegistryService serverMetaDataRegistryService,
            @ResultConverter MessageConverter<Object, ResultResponse> messageConverter) {
        this.contextConfig = Objects.requireNonNull(contextConfig, "contextConfig");
        this.enhancedDataSenderProvider = Objects.requireNonNull(enhancedDataSenderProvider, "enhancedDataSenderProvider");
        this.agentInfoFactoryProvider = Objects.requireNonNull(agentInfoFactoryProvider, "agentInfoFactoryProvider");
        this.serverMetaDataRegistryService = Objects.requireNonNull(serverMetaDataRegistryService, "serverMetaDataRegistryService");
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
    }

    @Override
    public AgentInfoSender get() {
        final EnhancedDataSender<MetaDataType> enhancedDataSender = this.enhancedDataSenderProvider.get();
        final AgentInfoFactory agentInfoFactory = this.agentInfoFactoryProvider.get();
        final AgentInfoSender agentInfoSender = new AgentInfoSender.Builder(enhancedDataSender, agentInfoFactory)
                .sendInterval(contextConfig.getAgentInfoSendRetryInterval())
                .setMessageConverter(this.messageConverter)
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