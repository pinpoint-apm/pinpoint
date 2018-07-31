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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultApplicationContext implements ApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final ModuleInstanceManager moduleInstanceManager;

    public DefaultApplicationContext(AgentOption agentOption, ModuleFactory moduleFactory) {
        Assert.requireNonNull(agentOption, "agentOption must not be null");
        Assert.requireNonNull(moduleFactory, "moduleFactory must not be null");
        Assert.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig must not be null");

        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }

        final PinpointModuleHolder pinpointModuleHolder = moduleFactory.newModule(agentOption);
        Injector injector = Guice.createInjector(Stage.PRODUCTION, pinpointModuleHolder.getModule());

        this.moduleInstanceManager = pinpointModuleHolder.getInstanceManager(injector);
    }

    @Override
    public void start() {
        this.moduleInstanceManager.start();
    }

    @Override
    public void close() {
        this.moduleInstanceManager.stop();
    }

    public ModuleInstanceHolder getModuleInstanceHolder() {
        return moduleInstanceManager.getModuleInstanceHolder();
    }

}
