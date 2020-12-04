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

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class GrpcAgentUriStatMapper {

    public AgentUriStatBo map(final PAgentUriStat agentUriStat) {
        final Header agentInfo = ServerContext.getAgentInfo();

        final String agentId = agentInfo.getAgentId();
        final long startTimestamp = agentInfo.getAgentStartTime();

        long timestamp = agentUriStat.getTimestamp();
        int bucketVersion = agentUriStat.getBucketVersion();

        AgentUriStatBo agentUriStatBo = new AgentUriStatBo();
        agentUriStatBo.setAgentId(agentId);
        agentUriStatBo.setStartTimestamp(startTimestamp);
        agentUriStatBo.setTimestamp(timestamp);
        agentUriStatBo.setBucketVersion(new Integer(bucketVersion).byteValue());

        List<PEachUriStat> eachUriStatList = agentUriStat.getEachUriStatList();
        for (PEachUriStat pEachUriStat : eachUriStatList) {
            EachUriStatBo eachUriStatBo = createEachUriStatBo(pEachUriStat);
            agentUriStatBo.addEachUriStatBo(eachUriStatBo);
        }

        return agentUriStatBo;
    }

    private EachUriStatBo createEachUriStatBo(PEachUriStat pEachUriStat) {
        EachUriStatBo eachUriStatBo = new EachUriStatBo();

        final String uri = pEachUriStat.getUri();
        eachUriStatBo.setUri(uri);

        PUriHistogram pTotalHistogram = pEachUriStat.getTotalHistogram();
        final UriStatHistogram totalHistogram = convertUriStatHistogram(pTotalHistogram);
        eachUriStatBo.setTotalHistogram(totalHistogram);

        PUriHistogram pFailedHistogram = pEachUriStat.getFailedHistogram();
        final UriStatHistogram failedHistogram = convertUriStatHistogram(pFailedHistogram);
        eachUriStatBo.setFailedHistogram(failedHistogram);

        return eachUriStatBo;
    }

    private UriStatHistogram convertUriStatHistogram(PUriHistogram pUriHistogram) {
        int count = pUriHistogram.getCount();
        if (count == 0) {
            return null;
        }

        double avg = pUriHistogram.getAvg();
        long max = pUriHistogram.getMax();

        int histogramCount = pUriHistogram.getHistogramCount();
        List<Integer> histogramList = pUriHistogram.getHistogramList();

        int[] histogram = new int[histogramCount];
        for (int i = 0; i < histogramCount; i++) {
            histogram[i] = histogramList.get(i);
        }

        UriStatHistogram uriStatHistogram = new UriStatHistogram();
        uriStatHistogram.setCount(count);
        uriStatHistogram.setAvg(avg);
        uriStatHistogram.setMax(max);
        uriStatHistogram.setTimestampHistogram(histogram);

        return uriStatHistogram;
    }

}
