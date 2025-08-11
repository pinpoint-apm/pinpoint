package com.navercorp.pinpoint.uristat.web.mapper;

import com.navercorp.pinpoint.uristat.web.entity.UriHistogramTotalEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriHistogramFailEntity;
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
        entity.setTot(10.0);

        UriStatChartValue model = mapper.toTotalChart(entity);

        assertEquals(entity.getTimestamp(), model.getTimestamp());
        assertEquals(entity.getVersion(), model.getVersion());
        assertEquals(1, model.getValues().size());
        assertEquals(10.0, model.getValues().get(0));
        assertEquals("bar", model.getChartType().name());
        assertEquals("count", model.getUnit());
    }

    @Test
    void testFailureEntityToModel() {
        UriHistogramFailEntity entity = new UriHistogramFailEntity();
        entity.setTimestamp(1000);
        entity.setVersion("version");
        entity.setFail(10.0);

        UriStatChartValue model = mapper.toFailureChart(entity);

        assertEquals(entity.getTimestamp(), model.getTimestamp());
        assertEquals(entity.getVersion(), model.getVersion());
        assertEquals(1, model.getValues().size());
        assertEquals(10.0, model.getValues().get(0));
        assertEquals("bar", model.getChartType().name());
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
        assertEquals("line", model.getChartType().name());
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
        assertEquals("line", model.getChartType().name());
        assertEquals("", model.getUnit());
    }
}