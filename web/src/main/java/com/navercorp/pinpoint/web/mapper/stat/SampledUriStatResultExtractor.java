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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.mapper.stat.sampling.AgentStatSamplingHandler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.EagerSamplingHandler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentUriStat;
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class SampledUriStatResultExtractor implements ResultsExtractor<List<SampledAgentUriStat>> {

    private final TimeWindow timeWindow;
    private final AgentStatMapper<AgentUriStatBo> rowMapper;
    private final AgentStatSampler<EachUriStatBo, SampledEachUriStatBo> sampler;

    public SampledUriStatResultExtractor(TimeWindow timeWindow, AgentStatMapper<AgentUriStatBo> rowMapper, AgentStatSampler<EachUriStatBo, SampledEachUriStatBo> sampler) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        this.rowMapper = rowMapper;
        this.sampler = sampler;
    }

    @Override
    public List<SampledAgentUriStat> extractData(ResultScanner results) throws Exception {
        // divide by uri
        Map<String, List<EachUriStatBo>> eachUriStatBoListMap = divideByUri(results);

        List<SampledAgentUriStat> result = new ArrayList<>(eachUriStatBoListMap.size());

        for (List<EachUriStatBo> eachUriStatBoList : eachUriStatBoListMap.values()) {
            result.add(getSampleData(eachUriStatBoList));
        }

        return result;
    }

    private Map<String, List<EachUriStatBo>> divideByUri(ResultScanner results) throws Exception {
        Map<String, List<EachUriStatBo>> eachUriStatBoListMap = new HashMap<>();

        int rowNum = 0;
        for (Result result : results) {
            for (AgentUriStatBo agentUriStatBo : this.rowMapper.mapRow(result, rowNum++)) {
                List<EachUriStatBo> eachUriStatBoList = agentUriStatBo.getEachUriStatBoList();
                if (CollectionUtils.isEmpty(eachUriStatBoList)) {
                    continue;
                }

                final String agentId = agentUriStatBo.getAgentId();
                final long startTimestamp = agentUriStatBo.getStartTimestamp();
                final long timestamp = agentUriStatBo.getTimestamp();

                for (EachUriStatBo eachUriStatBo : eachUriStatBoList) {
                    String uri = eachUriStatBo.getUri();

                    List<EachUriStatBo> eachUriStatBos = eachUriStatBoListMap.computeIfAbsent(uri, k -> new ArrayList<EachUriStatBo>());
                    setAgentStatDataPointBaseData(eachUriStatBo, agentId, startTimestamp, timestamp);

                    eachUriStatBos.add(eachUriStatBo);
                }
            }
        }
        return eachUriStatBoListMap;
    }

    private void setAgentStatDataPointBaseData(AgentStatDataPoint statDataPoint, String agentId, long startTimestamp, long timestamp) {
        statDataPoint.setAgentId(agentId);
        statDataPoint.setStartTimestamp(startTimestamp);
        statDataPoint.setTimestamp(timestamp);
    }

    private SampledAgentUriStat getSampleData(List<EachUriStatBo> eachUriStatBos) {
        eachUriStatBos.sort(Collections.reverseOrder(Comparator.comparingLong(EachUriStatBo::getTimestamp)));

        AgentStatSamplingHandler<EachUriStatBo, SampledEachUriStatBo> samplingHandler = new EagerSamplingHandler<>(timeWindow, sampler);
        for (EachUriStatBo eachUriStatBo : eachUriStatBos) {
            samplingHandler.addDataPoint(eachUriStatBo);
        }

        List<SampledEachUriStatBo> sampledDataPoints = samplingHandler.getSampledDataPoints();

        SampledAgentUriStat sampledAgentUriStat = new SampledAgentUriStat(sampledDataPoints);
        return sampledAgentUriStat;
    }

}

