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

package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellBuilderFactory;
import org.apache.hadoop.hbase.CellBuilderType;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author emeroad
 */
public class ResponseTimeMapperTest {

    @Test
    public void testResponseTimeMapperTest() {

        Buffer buffer = new AutomaticBuffer();
        HistogramSlot histogramSlot = ServiceType.STAND_ALONE.getHistogramSchema().findHistogramSlot(1000, false);
        short histogramSlotTime = histogramSlot.getSlotTime();
        buffer.putShort(histogramSlotTime);
        buffer.putBytes(Bytes.toBytes("agent"));
        byte[] bufferArray = buffer.getBuffer();
        byte[] valueArray = Bytes.toBytes(1L);

        Cell mockCell = CellBuilderFactory.create(CellBuilderType.SHALLOW_COPY)
                .setRow(HConstants.EMPTY_BYTE_ARRAY)
                .setFamily(HConstants.EMPTY_BYTE_ARRAY)
                .setQualifier(bufferArray)
                .setTimestamp(HConstants.LATEST_TIMESTAMP)
                .setType(Cell.Type.Put)
                .setValue(valueArray)
                .build();

        ResponseTimeMapper responseTimeMapper = new ResponseTimeMapper(mock(ServiceTypeRegistryService.class), mock(RowKeyDistributorByHashPrefix.class));
        ResponseTime responseTime = new ResponseTime("applicationName", ServiceType.STAND_ALONE, System.currentTimeMillis());
        responseTimeMapper.recordColumn(responseTime, mockCell);

        Histogram agentHistogram = responseTime.findHistogram("agent");

        assertThat(agentHistogram)
                .extracting(Histogram::getFastCount, Histogram::getNormalCount, Histogram::getSlowCount)
                .containsExactly(1L, 0L, 0L);
    }
}
