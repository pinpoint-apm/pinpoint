/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugins.loader.trace;

import com.navercorp.pinpoint.common.trace.LoadedTraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.plugins.loader.PinpointPluginLoader;
import com.navercorp.pinpoint.plugins.loader.trace.yaml.TraceMetadataProviderYamlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataProviderLoader implements PinpointPluginLoader<TraceMetadataProvider> {

    private final TraceMetadataProviderParser traceMetadataProviderParser = new TraceMetadataProviderYamlParser();

    @Override
    public List<TraceMetadataProvider> load(ClassLoader classLoader) {
        List<TraceMetadataProvider> traceMetadataProviders = new ArrayList<TraceMetadataProvider>();
        traceMetadataProviders.addAll(fromMetaFiles(classLoader));
        traceMetadataProviders.addAll(fromServiceLoader(classLoader));
        return traceMetadataProviders;
    }

    private List<TraceMetadataProvider> fromMetaFiles(ClassLoader classLoader) {
        return traceMetadataProviderParser.parse(classLoader);
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
