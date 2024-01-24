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

package com.navercorp.pinpoint.inspector.web.definition.metric.field;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 *
 */
@Component
public class FieldProcessorManager {
    private final Map<String, FieldPostProcessor> postProcessorMap = new HashMap<>();

    public FieldProcessorManager(List<FieldPostProcessor> postProcessorList) {
        for (FieldPostProcessor postProcessor : postProcessorList) {
            postProcessorMap.put(postProcessor.getName(), postProcessor);
        }
    }

    public FieldPostProcessor getPostProcessor(String name) {
        if (EmptyPostProcessor.INSTANCE.getName().equals(name)) {
            return EmptyPostProcessor.INSTANCE;
        }
        final FieldPostProcessor fieldPostProcessor = postProcessorMap.get(name);
        if (fieldPostProcessor == null) {
            throw new IllegalArgumentException("postProcessor not found. name:" + name);
        }

        return fieldPostProcessor;
    }

}
