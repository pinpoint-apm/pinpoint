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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.compress.SpanEventCompressor;
import com.navercorp.pinpoint.profiler.context.compress.SpanEventCompressorV2;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanChunkFactoryV2 implements SpanChunkFactory {

    private static final TraceDataFormatVersion V2 = TraceDataFormatVersion.V2;

    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;
    private final ServiceType applicationServiceType;

    private final SpanEventCompressor<Long> spanEventCompressor = new SpanEventCompressorV2();

    public SpanChunkFactoryV2(String applicationName, String agentId, long agentStartTime, ServiceType applicationServiceType) {

        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (applicationServiceType == null) {
            throw new NullPointerException("applicationServiceType must not be null");
        }

        this.applicationName = applicationName;
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.applicationServiceType = applicationServiceType;
    }

    @Override
    public SpanChunk create(TraceRoot traceRoot, final List<SpanEvent> spanEventList) {
        if (CollectionUtils.isEmpty(spanEventList)) {
            throw new IllegalArgumentException("spanEventList is empty.");
        }

        final SpanEvent first = spanEventList.get(0);
        if (first == null) {
            throw new IllegalStateException("first SpanEvent is null");
        }


        final SpanChunk spanChunk = new SpanChunk(spanEventList);
        spanChunk.setVersion(V2.getVersion());

        spanChunk.setAgentId(agentId);
        spanChunk.setApplicationName(applicationName);
        spanChunk.setAgentStartTime(agentStartTime);
        spanChunk.setApplicationServiceType(applicationServiceType.getCode());


        final long keyTime = first.getStartTime();
        spanChunk.setKeyTime(keyTime);
        // TODO change data compression timing to another thread  eg: DataSender thread
        spanEventCompressor.compress(spanEventList, keyTime);

        final ByteBuffer transactionId = traceRoot.getCompactTransactionId();
        spanChunk.setTransactionId(transactionId);

        final TraceId traceId = traceRoot.getTraceId();
        spanChunk.setSpanId(traceId.getSpanId());
        spanChunk.setEndPoint(traceRoot.getShared().getEndPoint());

        return spanChunk;
    }

}
