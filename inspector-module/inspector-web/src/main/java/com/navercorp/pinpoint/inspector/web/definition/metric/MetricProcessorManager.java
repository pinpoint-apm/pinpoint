/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.definition.metric;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
@Component
public class MetricProcessorManager {
    private final Map<String, MetricPreProcessor> preProcessorMap = new HashMap<>();
    private final Map<String, MetricPostProcessor> postProcessorMap = new HashMap<>();

    public MetricProcessorManager(List<MetricPostProcessor> postProcessorList, List<MetricPreProcessor> preProcessorList) {
        for (MetricPostProcessor postProcessor : postProcessorList) {
            postProcessorMap.put(postProcessor.getName(), postProcessor);
        }
        postProcessorMap.put(EmptyPostProcessor.INSTANCE.getName(), EmptyPostProcessor.INSTANCE);

        for(MetricPreProcessor preProcessor : preProcessorList) {
            preProcessorMap.put(preProcessor.getName(), preProcessor);
        }
        preProcessorMap.put(EmptyPreProcessor.INSTANCE.getName(), EmptyPreProcessor.INSTANCE);
    }

    public MetricPostProcessor getPostProcessor(String name) {
        final MetricPostProcessor metricPostProcessor = postProcessorMap.get(name);
        if (metricPostProcessor == null) {
            throw new IllegalArgumentException("postProcessor not found. name:" + name);
        }

        return metricPostProcessor;
    }

    public MetricPreProcessor getPreProcessor(String name) {
        final MetricPreProcessor metricPreProcessor = preProcessorMap.get(name);
        if (metricPreProcessor == null) {
            throw new IllegalArgumentException("preProcessor not found. name:" + name);
        }

        return metricPreProcessor;
    }
}
