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

package com.navercorp.pinpoint.test;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContextModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.PinpointModuleHolder;

/**
 * @author Woonduk Kang(emeroad)
 */
public class OverrideModuleFactory implements ModuleFactory {
    private final Module[] overrideModule;

    public OverrideModuleFactory(Module... overrideModule) {
        this.overrideModule = Assert.requireNonNull(overrideModule, "overrideModule must not be null");
    }

    @Override
    public PinpointModuleHolder newModule(AgentOption agentOption) {
        ModuleFactory moduleFactory = new ApplicationContextModuleFactory();
        PinpointModuleHolder pinpointModuleHolder = moduleFactory.newModule(agentOption);

        Module newModule = Modules.override(pinpointModuleHolder.getModule()).with(overrideModule);
        return new PinpointModuleHolder(newModule, agentOption.getInstrumentation());
    }

}
