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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.AgentContextOption;
import com.navercorp.pinpoint.profiler.context.config.ErrorRecorderConfig;
import com.navercorp.pinpoint.profiler.context.module.config.ConfigModule;
import com.navercorp.pinpoint.profiler.context.module.config.ConfigurationLoader;
import com.navercorp.pinpoint.profiler.context.monitor.config.DefaultExceptionTraceConfig;
import com.navercorp.pinpoint.profiler.context.monitor.config.ExceptionTraceConfig;
import com.navercorp.pinpoint.profiler.micrometer.MicrometerModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationContextModuleFactory implements ModuleFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public Module newModule(AgentContextOption agentOption) {
        final Module config = new ConfigModule(agentOption);
        final Module pluginModule = new PluginModule();
        final Module applicationContextModule = new ApplicationContextModule();
        final Module rpcModule = newRpcModule(agentOption);
        final Module statsModule = new StatsModule();
        final Module thriftStatsModule = new ThriftStatsModule();

        final ProfilerConfig properties = agentOption.getProfilerConfig();
        final Module micrometerModule = new MicrometerModule(properties::readString);

        final Module exceptionTraceModule = newExceptionTraceModule(properties.getProperties());
        final Module errorRecorderModule = newErrorRecorderModule(properties.getProperties());

        return Modules.combine(config,
                pluginModule,
                applicationContextModule,
                rpcModule,
                statsModule,
                thriftStatsModule,
                exceptionTraceModule,
                errorRecorderModule,
                micrometerModule);
    }

    private Module newErrorRecorderModule(Properties properties) {
        ConfigurationLoader configurationLoader = new ConfigurationLoader(properties);
        ErrorRecorderConfig errorRecorderConfig = new ErrorRecorderConfig();
        configurationLoader.load(errorRecorderConfig);
        logger.info("{}", errorRecorderConfig);

        if (errorRecorderConfig.isEnable()) {
            logger.info("load ConfigurableErrorRecorderModule");
            return new ConfigurableErrorRecorderModule(errorRecorderConfig);
        } else {
            logger.info("load SimpleErrorRecorderModule");
            return new SimpleErrorRecorderModule();
        }
    }

    protected Module newExceptionTraceModule(Properties properties) {
        ConfigurationLoader configurationLoader = new ConfigurationLoader(properties);
        ExceptionTraceConfig exceptionTraceConfig = new DefaultExceptionTraceConfig();
        configurationLoader.load(exceptionTraceConfig);
        logger.info("{}", exceptionTraceConfig);

        if (exceptionTraceConfig.isExceptionTraceEnable()) {
            logger.info("load ExceptionTraceModule");
            return new ExceptionTraceModule(exceptionTraceConfig);
        } else {
            logger.info("load DisabledExceptionTraceModule");
            return new DisabledExceptionTraceModule(exceptionTraceConfig);
        }
    }

    protected Module newRpcModule(AgentContextOption agentOption) {
        ProfilerConfig profilerConfig = agentOption.getProfilerConfig();
        logger.info("load GrpcModule");
        return new GrpcModule(profilerConfig);
    }
}
