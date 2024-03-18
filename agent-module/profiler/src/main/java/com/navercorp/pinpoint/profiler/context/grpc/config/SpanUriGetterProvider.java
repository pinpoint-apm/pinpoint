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
package com.navercorp.pinpoint.profiler.context.grpc.config;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;


/**
 * @author intr3p1d
 */
public class SpanUriGetterProvider implements Provider<SpanUriGetter> {

    public static final String SPAN_COLLECTED_URI_CONFIG = "profiler.span.collected.uri.type";

    public enum SpanUriType {
        TEMPLATE, RAW, AUTO
    }

    private final SpanUriType spanCollectedUriType;

    @Inject
    public SpanUriGetterProvider(ProfilerConfig profilerConfig) {
        Objects.requireNonNull(profilerConfig, "profilerConfig");
        spanCollectedUriType = SpanUriType.valueOf(profilerConfig.readString(SPAN_COLLECTED_URI_CONFIG, "AUTO"));

    }

    @Override
    public SpanUriGetter get() {
        switch (spanCollectedUriType) {
            case RAW:
                return new SpanRawUriGetter();
            case TEMPLATE:
                return new SpanTemplateUriGetter();
            default:
                return new SpanAutoUriGetter();
        }
    }
}
