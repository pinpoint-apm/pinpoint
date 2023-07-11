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
 *
 */

package com.navercorp.pinpoint.common.server.util.json;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;

public final class Jackson {

    private static final Module parameterNamesModule = getParameterNamesModule();

    private Jackson() {
    }

    @SuppressWarnings("unchecked")
    private static Module getParameterNamesModule() {
        try {
            Class<? extends Module> parameterNamesModuleClass = (Class<? extends Module>)
                    ClassUtils.forName("com.fasterxml.jackson.module.paramnames.ParameterNamesModule", Jackson.class.getClassLoader());
            Constructor<? extends Module> constructor = parameterNamesModuleClass.getConstructor(JsonCreator.Mode.class);
            return BeanUtils.instantiateClass(constructor, JsonCreator.Mode.DEFAULT);
        } catch (ReflectiveOperationException ignore) {
            return null;
        }
    }

    public static ObjectMapper newMapper() {
        Jackson2ObjectMapperBuilder builder = newBuilder();
        return builder.build();
    }

    public static Jackson2ObjectMapperBuilder newBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        if (parameterNamesModule != null ) {
            builder.modulesToInstall(parameterNamesModule);
        }
        builder.featuresToDisable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
        );
        return builder;
    }


    public static YAMLMapper newYamlMapper() {
        YAMLMapper mapper = new YAMLMapper();

        Jackson2ObjectMapperBuilder builder = Jackson.newBuilder();
        builder.configure(mapper);

        return mapper;
    }

}
