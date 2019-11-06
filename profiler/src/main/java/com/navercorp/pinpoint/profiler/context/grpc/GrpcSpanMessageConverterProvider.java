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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;


import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcSpanMessageConverterProvider implements Provider<MessageConverter<GeneratedMessageV3>> {

    private final String agentId;
    private final short applicationServiceTypeCode;

    private final SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanPostProcessor;

    @Inject
    public GrpcSpanMessageConverterProvider(@AgentId String agentId, @ApplicationServerType ServiceType applicationServiceType,
                                            SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanPostProcessor) {
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.applicationServiceTypeCode = applicationServiceType.getCode();
        this.spanPostProcessor = Assert.requireNonNull(spanPostProcessor, "spanPostProcessor");
    }

    @Override
    public MessageConverter<GeneratedMessageV3> get() {
        return new GrpcSpanMessageConverter(agentId, applicationServiceTypeCode, spanPostProcessor);
    }
}
