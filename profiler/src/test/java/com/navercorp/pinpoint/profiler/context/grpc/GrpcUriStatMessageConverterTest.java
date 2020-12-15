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

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.UriStatInfo;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Taejin Koo
 */
public class GrpcUriStatMessageConverterTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String[] URI_EXAMPLES = {"/index.html", "/main", "/error"};

    @Test
    public void convertTest() {
        long currentTimeMillis = System.currentTimeMillis();
        AgentUriStatData agentUriStatData = new AgentUriStatData(currentTimeMillis);

        List<UriStatInfo> uriStatInfoList = createRandomUriStatInfo(100);
        for (UriStatInfo uriStatInfo : uriStatInfoList) {
            agentUriStatData.add(uriStatInfo);
        }

        GrpcUriStatMessageConverter converter = new GrpcUriStatMessageConverter();
        PAgentUriStat agentUriStat = converter.toMessage(agentUriStatData);


        List<PEachUriStat> eachUriStatList = agentUriStat.getEachUriStatList();

        assertData(uriStatInfoList, eachUriStatList);
    }

    private List<UriStatInfo> createRandomUriStatInfo(int size) {
        List<UriStatInfo> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(createRandomUriStatInfo());
        }
        return result;
    }

    private UriStatInfo createRandomUriStatInfo() {
        int index = RANDOM.nextInt(URI_EXAMPLES.length);
        boolean status = RANDOM.nextBoolean();
        final int elapsedTime = RANDOM.nextInt(10000);
        return new UriStatInfo(URI_EXAMPLES[index], status, elapsedTime);
    }

    private void assertData(List<UriStatInfo> uriStatInfoList, List<PEachUriStat> eachUriStatList) {
        for (PEachUriStat pEachUriStat : eachUriStatList) {
            String uri = pEachUriStat.getUri();
            assertData(getUriStatInfo(uriStatInfoList, uri), pEachUriStat.getTotalHistogram());
            assertData(getFailedUriStatInfo(uriStatInfoList, uri), pEachUriStat.getFailedHistogram());
        }
    }

    private void assertData(List<UriStatInfo> expected, PUriHistogram actual) {
        Assert.assertEquals(expected.size(), actual.getCount());
        Assert.assertEquals(getMax(expected), actual.getMax());
        Assert.assertEquals(new Double(getAvg(expected)).longValue(), new Double(actual.getAvg()).longValue());

        List<Integer> histogramList = actual.getHistogramList();
        for (int i = 0; i < histogramList.size(); i++) {
            UriStatHistogramBucket valueByIndex = UriStatHistogramBucket.getValueByIndex(i);
            int bucketCount = getBucketCount(expected, valueByIndex);
            Assert.assertEquals(new Integer(bucketCount), histogramList.get(i));
        }
    }

    private long getMax(List<UriStatInfo> expected) {
        long max = 0;
        for (UriStatInfo uriStatInfo : expected) {
            max = Math.max(max, uriStatInfo.getElapsed());
        }
        return max;
    }

    private double getAvg(List<UriStatInfo> expected) {
        long total = 0l;
        for (UriStatInfo uriStatInfo : expected) {
            total += uriStatInfo.getElapsed();
        }
        return total / expected.size();
    }

    private int getBucketCount(List<UriStatInfo> uriStatInfoList, UriStatHistogramBucket type) {
        int count = 0;

        for (UriStatInfo uriStatInfo : uriStatInfoList) {
            UriStatHistogramBucket value = UriStatHistogramBucket.getValue(uriStatInfo.getElapsed());
            if (value == type) {
                count += 1;
            }
        }

        return count;
    }

    private List<UriStatInfo> getUriStatInfo(List<UriStatInfo> uriStatInfoList, String uri) {
        List<UriStatInfo> result = new ArrayList<>();
        for (UriStatInfo uriStatInfo : uriStatInfoList) {
            if (uriStatInfo.getUri().equals(uri)) {
                result.add(uriStatInfo);
            }
        }
        return result;
    }

    private List<UriStatInfo> getFailedUriStatInfo(List<UriStatInfo> uriStatInfoList, String uri) {
        List<UriStatInfo> result = new ArrayList<>();
        for (UriStatInfo uriStatInfo : uriStatInfoList) {
            if (!uriStatInfo.getUri().equals(uri)) {
                continue;
            }
            if (uriStatInfo.isStatus()) {
                continue;
            }
            result.add(uriStatInfo);
        }
        return result;
    }

}
