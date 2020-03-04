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

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.profiler.context.provider.plugin.AnnotationKeyRegistryServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.plugin.ServiceTypeRegistryServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.plugin.Slf4jCommonLoggerFactory;
import com.navercorp.pinpoint.profiler.context.provider.plugin.TraceMetadataLoaderProvider;
import com.navercorp.pinpoint.profiler.context.provider.plugin.TraceMetadataLoaderServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */

public class PluginModule extends PrivateModule {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        bind(CommonLoggerFactory.class).toInstance(new Slf4jCommonLoggerFactory());
        bind(TraceMetadataLoader.class).toProvider(TraceMetadataLoaderProvider.class);
        bind(TraceMetadataLoaderService.class).toProvider(TraceMetadataLoaderServiceProvider.class).in(Scopes.SINGLETON);

        bind(ServiceTypeRegistryService.class).toProvider(ServiceTypeRegistryServiceProvider.class).in(Scopes.SINGLETON);
        expose(ServiceTypeRegistryService.class);

        bind(AnnotationKeyRegistryService.class).toProvider(AnnotationKeyRegistryServiceProvider.class).in(Scopes.SINGLETON);
        expose(AnnotationKeyRegistryService.class);
    }
}
