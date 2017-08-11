/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactoryV1;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactoryV2;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanChunkFactoryProvider implements Provider<SpanChunkFactory> {

    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;
    private final ServiceType applicationServiceType;
    private final TraceDataFormatVersion version;
    private final TransactionIdEncoder transactionIdEncoder;


    @Inject
    public SpanChunkFactoryProvider(ProfilerConfig profilerConfig, @ApplicationName String applicationName, @AgentId String agentId, @AgentStartTime long agentStartTime,
                                    @ApplicationServerType ServiceType applicationServiceType, TransactionIdEncoder transactionIdEncoder) {

        this.applicationName = Assert.requireNonNull(applicationName, "applicationName must not be null");
        this.agentId = Assert.requireNonNull(agentId, "agentId must not be null");

        this.agentStartTime = agentStartTime;
        this.applicationServiceType = Assert.requireNonNull(applicationServiceType, "applicationServiceType must not be null");
        this.transactionIdEncoder = Assert.requireNonNull(transactionIdEncoder, "transactionIdEncoder must not be null");


        this.version = TraceDataFormatVersion.getTraceDataFormatVersion(profilerConfig);
    }


    @Override
    public SpanChunkFactory get() {
        if (this.version == TraceDataFormatVersion.V2) {
            return new SpanChunkFactoryV2(applicationName, agentId, agentStartTime, applicationServiceType, transactionIdEncoder);
        }
        if (this.version == TraceDataFormatVersion.V1) {
            return new SpanChunkFactoryV1(applicationName, agentId, agentStartTime, applicationServiceType, transactionIdEncoder);
        }
        throw new UnsupportedOperationException("unknown version :" + version);
    }
}
