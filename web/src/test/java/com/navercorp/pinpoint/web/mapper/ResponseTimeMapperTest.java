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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.junit.Assert;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
 * @author emeroad
 */
public class ResponseTimeMapperTest {

    @Test
    public void testResponseTimeMapperTest() throws Exception {

        Buffer buffer = new AutomaticBuffer();
        HistogramSlot histogramSlot = ServiceType.STAND_ALONE.getHistogramSchema().findHistogramSlot(1000, false);
        short histogramSlotTime = histogramSlot.getSlotTime();
        buffer.putShort(histogramSlotTime);
        buffer.putBytes(Bytes.toBytes("agent"));
        byte[] bufferArray = buffer.getBuffer();
        byte[] valueArray = Bytes.toBytes(1L);

        Cell mockCell = CellUtil.createCell(HConstants.EMPTY_BYTE_ARRAY, HConstants.EMPTY_BYTE_ARRAY, bufferArray, HConstants.LATEST_TIMESTAMP, KeyValue.Type.Maximum.getCode(), valueArray);

        ResponseTimeMapper responseTimeMapper = new ResponseTimeMapper();
        ResponseTime responseTime = new ResponseTime("applicationName", ServiceType.STAND_ALONE, System.currentTimeMillis());
        responseTimeMapper.recordColumn(responseTime, mockCell);

        Histogram agentHistogram = responseTime.findHistogram("agent");
        long fastCount = agentHistogram.getFastCount();
        Assert.assertEquals(fastCount, 1);
        long normal = agentHistogram.getNormalCount();
        Assert.assertEquals(normal, 0);
        long slow = agentHistogram.getSlowCount();
        Assert.assertEquals(slow, 0);

    }
}
