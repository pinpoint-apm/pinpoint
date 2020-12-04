/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.EachUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.UriStatHistogram;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;

import java.util.Collection;

/**
 * @author Taejin Koo
 */
public class GrpcUriStatMessageConverter implements MessageConverter<PAgentUriStat> {

    private final static PUriHistogram EMPTY_DETAILED_DATA_INSTANCE = PUriHistogram.getDefaultInstance();

    @Override
    public PAgentUriStat toMessage(Object message) {
        if (!(message instanceof AgentUriStatData)) {
            return null;
        }

        return createPAgentUriStat((AgentUriStatData) message);
    }

    private PAgentUriStat createPAgentUriStat(AgentUriStatData agentUriStatData) {
        long baseTimestamp = agentUriStatData.getBaseTimestamp();

        PAgentUriStat.Builder builder = PAgentUriStat.newBuilder();
        builder.setTimestamp(baseTimestamp);
        builder.setBucketVersion(UriStatHistogramBucket.getBucketVersion());

        Collection<EachUriStatData> allUriStatData = agentUriStatData.getAllUriStatData();
        for (EachUriStatData eachUriStatData : allUriStatData) {
            PEachUriStat pEachUriStat = createPEachUriStat(eachUriStatData);
            builder.addEachUriStat(pEachUriStat);
        }

        return builder.build();
    }

    private PEachUriStat createPEachUriStat(EachUriStatData eachUriStatData) {
        String uri = eachUriStatData.getUri();

        PEachUriStat.Builder builder = PEachUriStat.newBuilder();
        builder.setUri(uri);

        UriStatHistogram totalHistogram = eachUriStatData.getTotalHistogram();
        PUriHistogram totalPUriHistogram = createPUriHistogram(totalHistogram);
        builder.setTotalHistogram(totalPUriHistogram);

        UriStatHistogram failedHistogram = eachUriStatData.getFailedHistogram();
        PUriHistogram failedPUriHistogram = createPUriHistogram(failedHistogram);
        builder.setFailedHistogram(failedPUriHistogram);

        return builder.build();
    }


    private PUriHistogram createPUriHistogram(UriStatHistogram uriStatHistogram) {
        int count = uriStatHistogram.getCount();
        if (uriStatHistogram.getCount() == 0) {
            return EMPTY_DETAILED_DATA_INSTANCE;
        }

        PUriHistogram.Builder builder = PUriHistogram.newBuilder();

        long total = uriStatHistogram.getTotal();
        long max = uriStatHistogram.getMax();

        builder.setCount(count);
        builder.setAvg(total / count);
        builder.setMax(max);

        int[] timestampHistograms = uriStatHistogram.getTimestampHistogram();
        for (int eachTimestampHistogram : timestampHistograms) {
            builder.addHistogram(eachTimestampHistogram);
        }

        return builder.build();
    }

}
