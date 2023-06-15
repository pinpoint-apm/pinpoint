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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanAutoUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanRawUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanTemplateUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanUriGetter;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcSpanMessageConverterProvider implements Provider<MessageConverter<SpanType, GeneratedMessageV3>> {
    public static final String SPAN_COLLECTED_URI_CONFIG = "profiler.span.collected.uri.type";

    private final String agentId;
    private final short applicationServiceTypeCode;

    private final SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanPostProcessor;

    public enum SpanUriType {
        TEMPLATE, RAW, AUTO
    }

    private final SpanUriGetter spanUriGetter;


    @Inject
    public GrpcSpanMessageConverterProvider(@AgentId String agentId, @ApplicationServerType ServiceType applicationServiceType,
                                            SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanPostProcessor,
                                            ProfilerConfig profilerConfig) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.applicationServiceTypeCode = applicationServiceType.getCode();
        this.spanPostProcessor = Objects.requireNonNull(spanPostProcessor, "spanPostProcessor");
        Objects.requireNonNull(profilerConfig, "profilerConfig");
        SpanUriType spanCollectedUriType = SpanUriType.valueOf(profilerConfig.readString(SPAN_COLLECTED_URI_CONFIG, "AUTO"));
        this.spanUriGetter = getSpanUriGetter(spanCollectedUriType);
    }

    @Override
    public MessageConverter<SpanType, GeneratedMessageV3> get() {
        return new GrpcSpanMessageConverter(agentId, applicationServiceTypeCode, spanPostProcessor, spanUriGetter);
    }

    private SpanUriGetter getSpanUriGetter(SpanUriType spanCollectedUriType) {
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
