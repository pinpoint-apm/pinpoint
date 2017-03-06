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
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.metadata.DefaultApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApiMetaDataServiceProvider implements Provider<ApiMetaDataService> {
    private final String agentId;
    private final long agentStartTime;
    private final Provider<EnhancedDataSender> enhancedDataSenderProvider;

    @Inject
    public ApiMetaDataServiceProvider(@AgentId String agentId, @AgentStartTime long agentStartTime, Provider<EnhancedDataSender> enhancedDataSenderProvider) {
        if (enhancedDataSenderProvider == null) {
            throw new NullPointerException("enhancedDataSenderProvider must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.enhancedDataSenderProvider = enhancedDataSenderProvider;
    }

    @Override
    public ApiMetaDataService get() {
        final EnhancedDataSender enhancedDataSender = this.enhancedDataSenderProvider.get();
        return new DefaultApiMetaDataService(agentId, agentStartTime, enhancedDataSender);
    }
}
