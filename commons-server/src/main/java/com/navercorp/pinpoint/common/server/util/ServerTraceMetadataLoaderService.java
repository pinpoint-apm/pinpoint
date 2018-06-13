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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.plugin.Plugin;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.plugin.ServerPluginLoader;
import com.navercorp.pinpoint.common.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServerTraceMetadataLoaderService implements TraceMetadataLoaderService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TraceMetadataLoader loader;

    public ServerTraceMetadataLoaderService(CommonLoggerFactory commonLoggerFactory) {
        this(ClassUtils.getDefaultClassLoader(), commonLoggerFactory);
    }

    public ServerTraceMetadataLoaderService(ClassLoader classLoader, CommonLoggerFactory commonLoggerFactory) {
        Assert.requireNonNull(classLoader, "classLoader must not be null");
        Assert.requireNonNull(commonLoggerFactory, "commonLoggerFactory must not be null");

        PluginLoader pluginLoader = new ServerPluginLoader(classLoader);
        List<Plugin<TraceMetadataProvider>> traceMetadataProvider = pluginLoader.load(TraceMetadataProvider.class);

        List<TraceMetadataProvider> traceMetadataProviderList = new ArrayList<TraceMetadataProvider>();
        for (Plugin<TraceMetadataProvider> plugin : traceMetadataProvider) {
            traceMetadataProviderList.addAll(plugin.getInstanceList());
        }

        this.loader = new TraceMetadataLoader(commonLoggerFactory);
        loader.load(traceMetadataProviderList);
    }

    @Override
    public List<ServiceTypeInfo> getServiceTypeInfos() {
        return loader.getServiceTypeInfos();
    }

    @Override
    public List<AnnotationKey> getAnnotationKeys() {
        return loader.getAnnotationKeys();
    }
}
