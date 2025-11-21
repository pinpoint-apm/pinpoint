/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util.json;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public final class Jackson {

    private Jackson() {
    }


    public static ObjectMapper newMapper() {
        Jackson2ObjectMapperBuilder builder = newBuilder();
        return builder.build();
    }

    public static Jackson2ObjectMapperBuilder newBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        builder.featuresToDisable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
        );
        return builder;
    }


    @Deprecated
    public static YAMLMapper newYamlMapper() {
        YAMLMapper mapper = new YAMLMapper();

        Jackson2ObjectMapperBuilder builder = Jackson.newBuilder();
        builder.configure(mapper);

        return mapper;
    }

}
