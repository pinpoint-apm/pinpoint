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

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class GrpcAgentUriStatMapper {

    private static final String DEFAULT_SERVICE_NAME = "DEFAULT";

    public AgentUriStatBo map(ServerHeader header, final PAgentUriStat agentUriStat) {
        final String agentId = header.getAgentId();
        final String applicationName = header.getApplicationName();

        int bucketVersion = agentUriStat.getBucketVersion();
        List<PEachUriStat> eachUriStatList = agentUriStat.getEachUriStatList();

        List<EachUriStatBo> list = new ArrayList<>(eachUriStatList.size());
        for (PEachUriStat pEachUriStat : eachUriStatList) {
            EachUriStatBo eachUriStatBo = createEachUriStatBo(pEachUriStat);
            list.add(eachUriStatBo);
        }

        return new AgentUriStatBo(
                (byte) bucketVersion,
                DEFAULT_SERVICE_NAME,
                applicationName,
                agentId, list);
    }

    private EachUriStatBo createEachUriStatBo(PEachUriStat pEachUriStat) {

        final String uri = pEachUriStat.getUri();
        long timestamp = pEachUriStat.getTimestamp();

        PUriHistogram pTotalHistogram = pEachUriStat.getTotalHistogram();
        final UriStatHistogram totalHistogram = convertUriStatHistogram(pTotalHistogram);

        PUriHistogram pFailedHistogram = pEachUriStat.getFailedHistogram();
        final UriStatHistogram failedHistogram = convertUriStatHistogram(pFailedHistogram);

        return new EachUriStatBo(timestamp, uri, totalHistogram, failedHistogram);
    }

    private UriStatHistogram convertUriStatHistogram(PUriHistogram pUriHistogram) {
        int histogramCount = pUriHistogram.getHistogramCount();

        if (histogramCount <= 0) {
            return null;
        }

        long total = pUriHistogram.getTotal();
        long max = pUriHistogram.getMax();

        List<Integer> histogramList = pUriHistogram.getHistogramList();

        return new UriStatHistogram(total, max, histogramList);
    }

}
