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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import org.apache.thrift.TBase;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanThriftMessageConverterProvider implements Provider<MessageConverter<TBase<?, ?>>> {

    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;
    private final ServiceType applicationServiceType;
    private final TransactionIdEncoder transactionIdEncoder;
    private final SpanProcessor<TSpan, TSpanChunk> spanPostProcessor;

    @Inject
    public SpanThriftMessageConverterProvider(@ApplicationName String applicationName, @AgentId String agentId, @AgentStartTime long agentStartTime,
                                              @ApplicationServerType ServiceType applicationServiceType,
                                              TransactionIdEncoder transactionIdEncoder, SpanProcessor<TSpan, TSpanChunk> spanPostProcessor) {
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName");
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.applicationServiceType = Assert.requireNonNull(applicationServiceType, "applicationServiceType");
        this.transactionIdEncoder = Assert.requireNonNull(transactionIdEncoder, "transactionIdEncoder");
        this.spanPostProcessor = Assert.requireNonNull(spanPostProcessor, "spanPostProcessor");
    }

    @Override
    public MessageConverter<TBase<?, ?>> get() {
        return new SpanThriftMessageConverter(applicationName, agentId, agentStartTime, applicationServiceType.getCode(), transactionIdEncoder, spanPostProcessor);
    }
}
