/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.loader.plugins.trace;

import com.navercorp.pinpoint.common.trace.LoadedTraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.ParsedTraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.Filter;
import com.navercorp.pinpoint.loader.plugins.PinpointPluginLoader;
import com.navercorp.pinpoint.loader.plugins.trace.yaml.TraceMetadataProviderYamlParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author HyunGil Jeong
 * @author Taejin Koo
 */
public class TraceMetadataProviderLoader implements PinpointPluginLoader<TraceMetadataProvider> {

    private static final String TYPE_PROVIDER_DEF_ENTRY = "META-INF/pinpoint/type-provider.yml";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String typeProviderDefEntry;
    private final Collection<URL> customTypeProviderUrls;
    private final Filter<URL> pluginTypeProviderUrlFilter;
    private final TraceMetadataProviderParser traceMetadataProviderParser = new TraceMetadataProviderYamlParser();

    public TraceMetadataProviderLoader() {
        this(Collections.<URL>emptyList());
    }

    public TraceMetadataProviderLoader(Collection<URL> customTypeProviderUrls) {
        this(customTypeProviderUrls, new Filter<URL>() {
            @Override
            public boolean filter(URL value) {
                return NOT_FILTERED;
            }
        });
    }

    public TraceMetadataProviderLoader(Collection<URL> customTypeProviderUrls, Filter<URL> pluginTypeProviderUrlFilter) {
        this.typeProviderDefEntry = TYPE_PROVIDER_DEF_ENTRY;
        this.customTypeProviderUrls = Assert.requireNonNull(customTypeProviderUrls, "customTypeProviderUrls");
        this.pluginTypeProviderUrlFilter = Assert.requireNonNull(pluginTypeProviderUrlFilter, "pluginTypeProviderUrlFilter");
    }

    @Override
    public List<TraceMetadataProvider> load(ClassLoader classLoader) {
        List<TraceMetadataProvider> traceMetadataProviders = new ArrayList<TraceMetadataProvider>();
        traceMetadataProviders.addAll(fromMetaFiles(classLoader));
        traceMetadataProviders.addAll(fromServiceLoader(classLoader));
        return traceMetadataProviders;
    }

    private List<TraceMetadataProvider> fromMetaFiles(ClassLoader classLoader) {
        Set<String> loadedProviderIds = new HashSet<String>();
        List<TraceMetadataProvider> traceMetadataProviders = new ArrayList<TraceMetadataProvider>();
        for (URL typeProviderUrl : getTypeProviderUrls(classLoader)) {
            ParsedTraceMetadataProvider parsedTraceMetadataProvider = traceMetadataProviderParser.parse(typeProviderUrl);
            String loadedProviderId = parsedTraceMetadataProvider.getId();
            if (loadedProviderIds.contains(loadedProviderId)) {
                logger.debug("Skipping trace metadata provider from {} as provider id [{}] has already been added.", typeProviderUrl, loadedProviderId);
            } else {
                loadedProviderIds.add(loadedProviderId);
                traceMetadataProviders.add(parsedTraceMetadataProvider);
            }
        }
        return traceMetadataProviders;
    }

    private List<URL> getTypeProviderUrls(ClassLoader classLoader) {
        List<URL> typeProviderUrls = new ArrayList<URL>(customTypeProviderUrls);

        for (URL customTypeProviderUrl : customTypeProviderUrls) {
            if (pluginTypeProviderUrlFilter.filter(customTypeProviderUrl)) {
                continue;
            }
            typeProviderUrls.add(customTypeProviderUrl);
        }

        try {
            Enumeration<URL> pluginTypeProviderUrls = classLoader.getResources(typeProviderDefEntry);
            while (pluginTypeProviderUrls.hasMoreElements()) {
                URL pluginTypeProviderUrl = pluginTypeProviderUrls.nextElement();
                if (pluginTypeProviderUrlFilter.filter(pluginTypeProviderUrl)) {
                    continue;
                }

                typeProviderUrls.add(pluginTypeProviderUrl);
            }
        } catch (IOException e) {
            throw new IllegalStateException("I/O error getting type provider definitions", e);
        }
        return typeProviderUrls;
    }

    private List<TraceMetadataProvider> fromServiceLoader(ClassLoader classLoader) {
        List<TraceMetadataProvider> traceMetadataProviders = new ArrayList<TraceMetadataProvider>();
        ServiceLoader<TraceMetadataProvider> serviceLoader = ServiceLoader.load(TraceMetadataProvider.class, classLoader);
        for (TraceMetadataProvider traceMetadataProvider : serviceLoader) {
            traceMetadataProviders.add(new LoadedTraceMetadataProvider(traceMetadataProvider));
        }
        return traceMetadataProviders;
    }
}
