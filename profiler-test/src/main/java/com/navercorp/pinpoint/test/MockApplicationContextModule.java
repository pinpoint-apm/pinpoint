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

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.module.PluginClassLoader;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginContextLoader;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApplicationContextModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MockApplicationContextModule() {
    }


    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        bind(TraceDataFormatVersion.class).toInstance(TraceDataFormatVersion.V1);
        bind(StorageFactory.class).to(TestSpanStorageFactory.class);

        ServerMetaDataRegistryService serverMetaDataRegistryService = newServerMetaDataRegistryService();
        bind(ServerMetaDataRegistryService.class).toInstance(serverMetaDataRegistryService);

        ClassLoader defaultClassLoader = ClassLoaderUtils.getDefaultClassLoader();
        bind(ClassLoader.class).annotatedWith(PluginClassLoader.class).toInstance(defaultClassLoader);
        bind(PluginSetup.class).toProvider(MockPluginSetupProvider.class).in(Scopes.SINGLETON);
        bind(ProfilerPluginContextLoader.class).toProvider(MockProfilerPluginContextLoaderProvider.class).in(Scopes.SINGLETON);
        bind(PluginContextLoadResult.class).toProvider(MockPluginContextLoadResultProvider.class).in(Scopes.SINGLETON);
    }


    private ServerMetaDataRegistryService newServerMetaDataRegistryService() {
        List<String> vmArgs = RuntimeMXBeanUtils.getVmArgs();
        ServerMetaDataRegistryService serverMetaDataRegistryService = new DefaultServerMetaDataRegistryService(vmArgs);
        return serverMetaDataRegistryService;
    }

}
