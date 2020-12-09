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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;
import com.navercorp.pinpoint.web.vo.stat.SampledUriStatHistogramBo;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
@Component
public class AgentUriStatSampler implements AgentStatSampler<EachUriStatBo, SampledEachUriStatBo> {

    private static final AgentStatPointFactory AGENT_STAT_POINT_FACTORY = new AgentStatPointFactory(0, 0L, 0D);

    private static Map<UriStatHistogramBucket, Integer> EMPTY_URI_STAT_HISTOGRAM_MAP;

    static {
        Map<UriStatHistogramBucket, Integer> map = new HashMap<>();
        for (UriStatHistogramBucket value : UriStatHistogramBucket.values()) {
            map.put(value, 0);
        }
        EMPTY_URI_STAT_HISTOGRAM_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public SampledEachUriStatBo sampleDataPoints(int index, long timestamp, List<EachUriStatBo> eachUriStatBoList, EachUriStatBo previousDataPoint) {
        if (CollectionUtils.isEmpty(eachUriStatBoList)) {
            return null;
        }

        final String uri = getUri(eachUriStatBoList);

        List<UriStatHistogram> totalUriStatHistogramList = eachUriStatBoList.stream().map(EachUriStatBo::getTotalHistogram).filter(h -> Objects.nonNull(h)).collect(Collectors.toList());
        SampledUriStatHistogramBo sampledTotalUriStatHistogramBo = create(timestamp, totalUriStatHistogramList);

        List<UriStatHistogram> failedUriStatHistogramList = eachUriStatBoList.stream().map(EachUriStatBo::getFailedHistogram).filter(h -> Objects.nonNull(h)).collect(Collectors.toList());
        SampledUriStatHistogramBo failedSampledUriStatHistogramBo = create(timestamp, failedUriStatHistogramList);

        SampledEachUriStatBo sampledEachUriStatBo = new SampledEachUriStatBo(uri, sampledTotalUriStatHistogramBo, failedSampledUriStatHistogramBo);
        return sampledEachUriStatBo;
    }

    private String getUri(List<EachUriStatBo> eachUriStatBoList) {
        EachUriStatBo representative = ListUtils.getFirst(eachUriStatBoList);
        return representative.getUri();
    }

    private SampledUriStatHistogramBo create(long timestamp, List<UriStatHistogram> uriStatHistogramList) {
        if (CollectionUtils.isEmpty(uriStatHistogramList)) {
            return createEmptySampledUriStatHistogramBo(timestamp);
        }

        final List<Integer> countList = uriStatHistogramList.stream().map(UriStatHistogram::getCount).collect(Collectors.toList());
        final AgentStatPoint<Integer> countPoint = AGENT_STAT_POINT_FACTORY.createIntPoint(timestamp, countList);

        final List<Long> maxElapsedTimeList = uriStatHistogramList.stream().map(UriStatHistogram::getMax).collect(Collectors.toList());
        final AgentStatPoint<Long> maxElapsedTimePoint = AGENT_STAT_POINT_FACTORY.createLongPoint(timestamp, maxElapsedTimeList);

        final List<Double> avgElapsedTimeList = uriStatHistogramList.stream().map(UriStatHistogram::getAvg).collect(Collectors.toList());
        final AgentStatPoint<Double> avgElapsedTimePoint = AGENT_STAT_POINT_FACTORY.createDoublePoint(timestamp, avgElapsedTimeList, 3);

        final Map<UriStatHistogramBucket, Integer> uriStatHistogramCountMap = createHistogramBucketCountMap(uriStatHistogramList);

        long totalElapsedTime = 0;
        for (int i = 0; i < countList.size(); i++) {
            totalElapsedTime += (countList.get(i) * avgElapsedTimeList.get(i));
        }

        SampledUriStatHistogramBo sampledUriStatHistogramBo = new SampledUriStatHistogramBo(countPoint, maxElapsedTimePoint, avgElapsedTimePoint, uriStatHistogramCountMap, totalElapsedTime);
        return sampledUriStatHistogramBo;
    }

    private Map<UriStatHistogramBucket, Integer> createHistogramBucketCountMap(List<UriStatHistogram> uriStatHistogramList) {
        int[] mergedHistogramValue = UriStatHistogramBucket.createNewArrayValue();
        for (UriStatHistogram uriStatHistogram : uriStatHistogramList) {
            int[] timestampHistogram = uriStatHistogram.getTimestampHistogram();
            for (int i = 0; i < mergedHistogramValue.length; i++) {
                mergedHistogramValue[i] += timestampHistogram[i];
            }
        }

        Map<UriStatHistogramBucket, Integer> uriStatHistogramBucketCountMap = new EnumMap<UriStatHistogramBucket, Integer>(UriStatHistogramBucket.class);
        for (UriStatHistogramBucket value : UriStatHistogramBucket.values()) {
            int eachBucketTotalCount = mergedHistogramValue[value.getIndex()];
            uriStatHistogramBucketCountMap.put(value, eachBucketTotalCount);
        }
        return uriStatHistogramBucketCountMap;
    }

    private SampledUriStatHistogramBo createEmptySampledUriStatHistogramBo(long timestamp) {
        AgentStatPoint<Integer> emptyIntegerPoint = AGENT_STAT_POINT_FACTORY.createIntPoint(timestamp, Collections.emptyList());
        AgentStatPoint<Long> emptyLongPoint = AGENT_STAT_POINT_FACTORY.createLongPoint(timestamp, Collections.emptyList());
        AgentStatPoint<Double> emptyDoublePoint = AGENT_STAT_POINT_FACTORY.createDoublePoint(timestamp, Collections.emptyList());

        SampledUriStatHistogramBo sampledUriStatHistogramBo = new SampledUriStatHistogramBo(emptyIntegerPoint, emptyLongPoint, emptyDoublePoint, EMPTY_URI_STAT_HISTOGRAM_MAP, 0L);
        return sampledUriStatHistogramBo;
    }

}

