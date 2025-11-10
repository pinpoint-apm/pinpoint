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

package com.navercorp.pinpoint.uristat.web.mapper;

import com.navercorp.pinpoint.uristat.web.entity.UriHistogramFailEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriHistogramTotalEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriLatencyChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class EntityToModelMapperTest {

    private final EntityToModelMapper mapper = Mappers.getMapper(EntityToModelMapper.class);

    @Test
    void toModel() {
        UriStatSummaryEntity entity = new UriStatSummaryEntity();
        entity.setUri("uri");
        entity.setTotalCount(100.0);
        entity.setFailureCount(10.0);
        entity.setMaxTimeMs(200.0);
        entity.setVersion("version");
        entity.setTotalApdexRaw(50.0);
        entity.setSumOfTotalTimeMs(1000.0);

        UriStatSummary model = mapper.toModel(entity);

        assertEquals(entity.getUri(), model.getUri());
        assertEquals(entity.getTotalCount(), model.getTotalCount());
        assertEquals(entity.getFailureCount(), model.getFailureCount());
        assertEquals(entity.getMaxTimeMs(), model.getMaxTimeMs());
        assertEquals(entity.getVersion(), model.getVersion());
        assertEquals(0.5, model.getApdex());
        assertEquals(10.0, model.getAvgTimeMs());
    }

    @Test
    void testTotalEntityToModel() {
        UriHistogramTotalEntity entity = new UriHistogramTotalEntity();
        entity.setTimestamp(1000);
        entity.setVersion("version");
        entity.setTot0(10.0);
        entity.setTot1(20.0);
        entity.setTot2(30.0);
        entity.setTot3(40.0);
        entity.setTot4(50.0);
        entity.setTot5(60.0);
        entity.setTot6(70.0);
        entity.setTot7(80.0);

        UriStatChartValue model = mapper.toTotalChart(entity);

        assertEquals(entity.getTimestamp(), model.getTimestamp());
        assertEquals(entity.getVersion(), model.getVersion());
        assertEquals(8, model.getValues().size());
        assertEquals(10.0, model.getValues().get(0));
        assertEquals(20.0, model.getValues().get(1));
        assertEquals(30.0, model.getValues().get(2));
        assertEquals(40.0, model.getValues().get(3));
        assertEquals(50.0, model.getValues().get(4));
        assertEquals(60.0, model.getValues().get(5));
        assertEquals(70.0, model.getValues().get(6));
        assertEquals(80.0, model.getValues().get(7));
        assertEquals("bar", model.getChartType().getName());
        assertEquals("count", model.getUnit());
    }

    @Test
    void testFailureEntityToModel() {
        UriHistogramFailEntity entity = new UriHistogramFailEntity();
        entity.setTimestamp(1000);
        entity.setVersion("version");
        entity.setFail0(10.0);
        entity.setFail1(20.0);
        entity.setFail2(30.0);
        entity.setFail3(40.0);
        entity.setFail4(50.0);
        entity.setFail5(60.0);
        entity.setFail6(70.0);
        entity.setFail7(80.0);

        UriStatChartValue model = mapper.toFailureChart(entity);

        assertEquals(entity.getTimestamp(), model.getTimestamp());
        assertEquals(entity.getVersion(), model.getVersion());
        assertEquals(8, model.getValues().size());
        assertEquals(10.0, model.getValues().get(0));
        assertEquals(20.0, model.getValues().get(1));
        assertEquals(30.0, model.getValues().get(2));
        assertEquals(40.0, model.getValues().get(3));
        assertEquals(50.0, model.getValues().get(4));
        assertEquals(60.0, model.getValues().get(5));
        assertEquals(70.0, model.getValues().get(6));
        assertEquals(80.0, model.getValues().get(7));
        assertEquals("bar", model.getChartType().getName());
        assertEquals("count", model.getUnit());
    }

    @Test
    void testLatencyEntityToModel() {
        UriLatencyChartEntity entity = new UriLatencyChartEntity();
        entity.setTimestamp(1000);
        entity.setVersion("version");
        entity.setTotalTimeMs(1200.0);
        entity.setMaxLatencyMs(500.0);
        entity.setCount(3.0);

        UriStatChartValue model = mapper.toSimpleLatencyChart(entity);

        assertEquals(entity.getTimestamp(), model.getTimestamp());
        assertEquals(entity.getVersion(), model.getVersion());
        assertEquals(2, model.getValues().size());
        assertEquals(400.0, model.getValues().get(0));
        assertEquals(500.0, model.getValues().get(1));
        assertEquals("line", model.getChartType().getName());
        assertEquals("ms", model.getUnit());
    }

    @Test
    void testApdexEntityToModel() {
        UriStatChartEntity entity = new UriStatChartEntity();
        entity.setTimestamp(1000);
        entity.setVersion("version");
        entity.setApdexRaw(50.0);
        entity.setCount(100.0);

        UriStatChartValue model = mapper.toApdexChart(entity);

        assertEquals(entity.getTimestamp(), model.getTimestamp());
        assertEquals(entity.getVersion(), model.getVersion());
        assertEquals(1, model.getValues().size());
        assertEquals(0.5, model.getValues().get(0));
        assertEquals("line", model.getChartType().getName());
        assertEquals("", model.getUnit());
    }
}