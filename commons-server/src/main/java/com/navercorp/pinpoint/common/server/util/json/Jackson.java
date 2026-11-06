/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util.json;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.datatype.eclipsecollections.EclipseCollectionsModule;

/**
 * Jackson 3 mapper factory.
 * <p>
 * Every mapper keeps the Jackson 2 wire behavior the web API and the stored JSON were built against
 * (declaration-order properties, enum names, lenient primitives, ...); see {@link #configureJackson2Compatible}.
 */
public final class Jackson {
    private Jackson() {
    }

    public static JsonMapper newMapper() {
        return newBuilder().build();
    }

    public static JsonMapper.Builder newBuilder() {
        JsonMapper.Builder builder = JsonMapper.builder();
        configureJackson2Compatible(builder);
        builder.addModule(new EclipseCollectionsModule());
        return builder;
    }

    public static YAMLMapper newYamlMapper() {
        YAMLMapper.Builder builder = YAMLMapper.builder();
        configureJackson2Compatible(builder);
        builder.addModule(new EclipseCollectionsModule());
        return builder.build();
    }

    /**
     * Jackson 2 defaults plus the Spring Boot adjustments pinpoint has always run with:
     * ISO-8601 dates, unknown properties ignored, no default view inclusion.
     * Parameter-name based creators stay enabled; Jackson 2 got them from jackson-module-parameter-names.
     */
    public static <M extends ObjectMapper, B extends MapperBuilder<M, B>> B configureJackson2Compatible(B builder) {
        return builder.configureForJackson2()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .enable(MapperFeature.DETECT_PARAMETER_NAMES);
    }
}
