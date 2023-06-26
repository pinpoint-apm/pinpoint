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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.TransportModule;
import com.navercorp.pinpoint.profiler.context.module.config.ConfigModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationContextModuleFactory implements ModuleFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public Module newModule(AgentOption agentOption) {
        final Module config = new ConfigModule(agentOption);
        final Module pluginModule = new PluginModule();
        final Module applicationContextModule = new ApplicationContextModule();
        final Module rpcModule = newRpcModule(agentOption);
        final Module statsModule = new StatsModule();
        final Module thriftStatsModule = new ThriftStatsModule();

        final Properties properties = agentOption.getProfilerConfig().getProperties();
        final Module exceptionTraceModule = new ExceptionTraceModule(properties);

        return Modules.combine(config, pluginModule, applicationContextModule,
                rpcModule,
                statsModule, thriftStatsModule,
                exceptionTraceModule);
    }

    protected Module newRpcModule(AgentOption agentOption) {
        ProfilerConfig profilerConfig = agentOption.getProfilerConfig();
        final TransportModule transportModule = profilerConfig.getTransportModule();
        if (TransportModule.GRPC == transportModule) {
            logger.info("load GrpcModule");
            return new GrpcModule(profilerConfig);
        }
        logger.info("load GrpcModule");
        return new GrpcModule(profilerConfig);
    }
}
