/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.util.AgentInfoFactory;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoFactoryProvider implements Provider<AgentInfoFactory> {

    private final AgentInformation agentInformation;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;
    private final JvmInformation jvmInformation;

    @Inject
    public AgentInfoFactoryProvider(AgentInformation agentInformation, ServerMetaDataRegistryService serverMetaDataRegistryService, JvmInformation jvmInformation) {
        this.agentInformation = Assert.requireNonNull(agentInformation, "agentInformation");
        this.serverMetaDataRegistryService = Assert.requireNonNull(serverMetaDataRegistryService, "serverMetaDataRegistryService");
        this.jvmInformation = Assert.requireNonNull(jvmInformation, "jvmInformation");
    }

    @Override
    public AgentInfoFactory get() {
        return new AgentInfoFactory(agentInformation, serverMetaDataRegistryService, jvmInformation);
    }
}
