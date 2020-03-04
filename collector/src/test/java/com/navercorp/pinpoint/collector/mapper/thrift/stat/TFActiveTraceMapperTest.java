/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTrace;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTraceHistogram;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class TFActiveTraceMapperTest {

    public static final String TEST_AGENT = "test_agent";
    public static final long startTimestamp = 1496370596375L;
    public static final long timestamp = 1496470596375L;


    @Test
    public void mapTest() {
        ActiveTraceBo activeTraceBo = new ActiveTraceBo();
        activeTraceBo.setAgentId(TEST_AGENT);
        activeTraceBo.setVersion((short)1);
        activeTraceBo.setStartTimestamp(startTimestamp);
        activeTraceBo.setTimestamp(timestamp);
        activeTraceBo.setHistogramSchemaType(1);

        ActiveTraceHistogram activeTraceHistogram = new ActiveTraceHistogram(30, 40, 10, 50);
        activeTraceBo.setActiveTraceHistogram(activeTraceHistogram);

        TFActiveTraceMapper mapper = new TFActiveTraceMapper();
        TFActiveTrace tFActiveTrace = mapper.map(activeTraceBo);

        TFActiveTraceHistogram histogram = tFActiveTrace.getHistogram();
        assertEquals(1, histogram.getVersion());
        assertEquals(1, histogram.getHistogramSchemaType());

        List<Integer> activeTraceCountList = histogram.getActiveTraceCount();
        assertEquals(4, activeTraceCountList.size());
        assertEquals(30, (int)activeTraceCountList.get(0));
        assertEquals(40, (int)activeTraceCountList.get(1));
        assertEquals(10, (int)activeTraceCountList.get(2));
        assertEquals(50, (int)activeTraceCountList.get(3));
    }

}