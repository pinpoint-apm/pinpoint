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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Random;

/**
 * @author Taejin Koo
 */
public class GrpcUriStatMessageConverterTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final String[] URI_EXAMPLES = {"/index.html", "/main", "/error"};

    private final UriStatHistogramBucket.Layout layout = UriStatHistogramBucket.getLayout();

    @Test
    public void convertTest() {
        long currentTimeMillis = System.currentTimeMillis();
        AgentUriStatData agentUriStatData = new AgentUriStatData(currentTimeMillis, 10);

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
        final long timestamp = System.currentTimeMillis();
        return new UriStatInfo(URI_EXAMPLES[index], status, timestamp - elapsedTime, timestamp);
    }

    private void assertData(List<UriStatInfo> uriStatInfoList, List<PEachUriStat> eachUriStatList) {
        for (PEachUriStat pEachUriStat : eachUriStatList) {
            String uri = pEachUriStat.getUri();
            assertData(getUriStatInfo(uriStatInfoList, uri), pEachUriStat.getTotalHistogram());
            assertData(getFailedUriStatInfo(uriStatInfoList, uri), pEachUriStat.getFailedHistogram());
        }
    }

    private void assertData(List<UriStatInfo> expected, PUriHistogram actual) {
        LongSummaryStatistics summary = getSummary(expected);

        Assertions.assertEquals(summary.getCount(), actual.getCount());
        Assertions.assertEquals(summary.getMax(), actual.getMax());
        Assertions.assertEquals(summary.getSum(), actual.getTotal());

        List<Integer> histogramList = actual.getHistogramList();
        for (int i = 0; i < histogramList.size(); i++) {
            UriStatHistogramBucket valueByIndex = layout.getBucketByIndex(i);
            int bucketCount = getBucketCount(expected, valueByIndex);
            Assertions.assertEquals(new Integer(bucketCount), histogramList.get(i));
        }
    }

    private LongSummaryStatistics getSummary(List<UriStatInfo> expected) {
        return expected.stream()
                .mapToLong(UriStatInfo::getElapsed)
                .summaryStatistics();
    }


    private int getBucketCount(List<UriStatInfo> uriStatInfoList, UriStatHistogramBucket type) {
        int count = 0;

        for (UriStatInfo uriStatInfo : uriStatInfoList) {
            UriStatHistogramBucket value = layout.getBucket(uriStatInfo.getElapsed());
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
