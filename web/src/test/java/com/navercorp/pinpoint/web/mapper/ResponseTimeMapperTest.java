/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.HistogramSlot;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.mapper.ResponseTimeMapper;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import junit.framework.Assert;

import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
 * @author emeroad
 */
public class ResponseTimeMapperTest {

    @Test
    public void testResponseTimeMapperTest() throws Exception {
        ResponseTimeMapper responseTimeMapper = new ResponseTimeMapper();
        ResponseTime responseTime = new ResponseTime("applicationName", ServiceType.STAND_ALONE, System.currentTimeMillis());

        Buffer buffer = new AutomaticBuffer();
        HistogramSlot histogramSlot = ServiceType.STAND_ALONE.getHistogramSchema().findHistogramSlot(1000);
        short histogramSlotTime = histogramSlot.getSlotTime();
        buffer.put(histogramSlotTime);
        buffer.put(Bytes.toBytes("agent"));

        responseTimeMapper.recordColumn(responseTime, buffer.getBuffer(), Bytes.toBytes(1L), 0);

        Histogram agentHistogram = responseTime.findHistogram("agent");
        long fastCount = agentHistogram.getFastCount();
        Assert.assertEquals(fastCount, 1);
        long normal = agentHistogram.getNormalCount();
        Assert.assertEquals(normal, 0);
        long slow = agentHistogram.getSlowCount();
        Assert.assertEquals(slow, 0);

    }
}
