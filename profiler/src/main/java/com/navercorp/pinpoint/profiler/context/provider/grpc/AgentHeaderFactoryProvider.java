/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.profiler.AgentInformation;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentHeaderFactoryProvider implements Provider<HeaderFactory> {
    private final AgentInformation agentInformation;

    @Inject
    public AgentHeaderFactoryProvider(AgentInformation agentInformation) {
        this.agentInformation = Assert.requireNonNull(agentInformation, "agentInformation");
    }

    @Override
    public HeaderFactory get() {
        return new AgentHeaderFactory(agentInformation.getAgentId(), agentInformation.getApplicationName(), agentInformation.getStartTime());
    }
}
