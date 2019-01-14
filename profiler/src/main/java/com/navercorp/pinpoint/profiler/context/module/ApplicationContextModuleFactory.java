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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.profiler.context.module.config.ConfigModule;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationContextModuleFactory implements ModuleFactory {

    @Override
    public Module newModule(AgentOption agentOption) {
        final Module config = new ConfigModule(agentOption);
        final Module pluginModule = new PluginModule();
        final Module applicationContextModule = new ApplicationContextModule();
        final Module rpcModule = new RpcModule();
        final Module statsModule = new StatsModule();
        final Module thriftStatsModule = new ThriftStatsModule();

        return Modules.combine(config, pluginModule, applicationContextModule, rpcModule, statsModule, thriftStatsModule);
    }
}
