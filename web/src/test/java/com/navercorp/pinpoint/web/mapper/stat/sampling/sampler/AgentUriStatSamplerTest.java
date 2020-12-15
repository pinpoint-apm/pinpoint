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
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;
import com.navercorp.pinpoint.web.vo.stat.SampledUriStatHistogramBo;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class AgentUriStatSamplerTest {

    @Test
    public void sampleTest() {
        AgentUriStatSampler sampler = new AgentUriStatSampler();

        long currentTimeMillis = System.currentTimeMillis();
        long startTimestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(30);

        List<EachUriStatBo> eachUriStatBoList = new ArrayList<>();
        eachUriStatBoList.add(createEachUriStatBo("agentId", startTimestamp, currentTimeMillis, "/index.html"));
        eachUriStatBoList.add(createEachUriStatBo("agentId", startTimestamp, currentTimeMillis, "/index.html"));

        SampledEachUriStatBo sampledEachUriStatBo = sampler.sampleDataPoints(0, System.currentTimeMillis(), eachUriStatBoList, null);

        SampledUriStatHistogramBo totalSampledUriStatHistogramBo = sampledEachUriStatBo.getTotalSampledUriStatHistogramBo();

        AgentStatPoint<Long> maxTimePoint = totalSampledUriStatHistogramBo.getMaxTimePoint();
        assertMaxValue(maxTimePoint, eachUriStatBoList);

        AgentStatPoint<Integer> countPoint = totalSampledUriStatHistogramBo.getCountPoint();
        assertCountValue(countPoint, eachUriStatBoList);

        int[] uriStatHistogramValue = totalSampledUriStatHistogramBo.getUriStatHistogramValue();
        assertHistogramValue(uriStatHistogramValue, eachUriStatBoList);
    }

    private EachUriStatBo createEachUriStatBo(String agentId, long startTimestamp, long timestamp, String uri) {
        EachUriStatBo eachUriStatBo = new EachUriStatBo();
        eachUriStatBo.setAgentId(agentId);
        eachUriStatBo.setStartTimestamp(startTimestamp);
        eachUriStatBo.setTimestamp(timestamp);
        eachUriStatBo.setUri(uri);
        eachUriStatBo.setTotalHistogram(createUriStatHistogram(ThreadLocalRandom.current().nextInt(1, 10)));
        return eachUriStatBo;
    }

    private UriStatHistogram createUriStatHistogram(int count) {
        long totalElapsed = 0;
        long max = 0;
        int[] bucketValues = UriStatHistogramBucket.createNewArrayValue();
        for (int i = 0; i < count; i++) {
            int elapsed = ThreadLocalRandom.current().nextInt(10000);
            totalElapsed += elapsed;
            max = Math.max(max, elapsed);

            UriStatHistogramBucket bucket = UriStatHistogramBucket.getValue(elapsed);
            bucketValues[bucket.getIndex()] += 1;
        }

        UriStatHistogram uriStatHistogram = new UriStatHistogram();
        uriStatHistogram.setCount(count);
        uriStatHistogram.setAvg(totalElapsed / count);
        uriStatHistogram.setMax(max);
        uriStatHistogram.setTimestampHistogram(bucketValues);

        return uriStatHistogram;
    }

    private void assertMaxValue(AgentStatPoint<Long> expected, List<EachUriStatBo> actual) {
        Long max = actual.stream().mapToLong(o -> o.getTotalHistogram().getMax()).max().getAsLong();
        Assert.assertEquals(expected.getMaxYVal(), max);
    }

    private void assertCountValue(AgentStatPoint<Integer> expected, List<EachUriStatBo> actual) {
        Integer count = actual.stream().mapToInt(o -> o.getTotalHistogram().getCount()).sum();
        Assert.assertEquals(expected.getSumYVal(), count);
    }

    private void assertHistogramValue(int[] uriStatHistogramValue, List<EachUriStatBo> actual) {
        int[] newArrayValue = UriStatHistogramBucket.createNewArrayValue();
        for (EachUriStatBo eachUriStatBo : actual) {
            int[] timestampHistogram = eachUriStatBo.getTotalHistogram().getTimestampHistogram();
            for (int i = 0; i < timestampHistogram.length; i++) {
                newArrayValue[i] += timestampHistogram[i];
            }
        }

        Assert.assertTrue(Arrays.equals(uriStatHistogramValue, newArrayValue));
    }

}
