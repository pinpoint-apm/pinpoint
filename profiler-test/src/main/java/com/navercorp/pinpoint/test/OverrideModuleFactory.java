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

package com.navercorp.pinpoint.test;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.TransportModule;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContextModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.test.rpc.MockRpcModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class OverrideModuleFactory implements ModuleFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Module[] overrideModule;

    public OverrideModuleFactory(Module... overrideModule) {
        this.overrideModule = Assert.requireNonNull(overrideModule, "overrideModule");
    }

    @Override
    public Module newModule(AgentOption agentOption) {

        DefaultProfilerConfig profilerConfig = (DefaultProfilerConfig) agentOption.getProfilerConfig();
        profilerConfig.setTransportModule(TransportModule.THRIFT);

        ModuleFactory moduleFactory = new ApplicationContextModuleFactory() {
            @Override
            protected Module newRpcModule(AgentOption agentOption) {
                logger.info("load {}", MockRpcModule.class.getName());
                return new MockRpcModule();
            }
        };
        Module module = moduleFactory.newModule(agentOption);
        return Modules.override(module).with(overrideModule);
    }
}
