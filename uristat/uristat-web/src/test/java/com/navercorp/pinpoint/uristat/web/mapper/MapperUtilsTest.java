package com.navercorp.pinpoint.uristat.web.mapper;

import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperUtilsTest {

    @Test
    void groupByUriAndVersion() {
        UriStatSummaryEntity entity0 = new UriStatSummaryEntity();
        entity0.setUri("uri0");
        entity0.setVersion("version0");

        UriStatSummaryEntity entity1 = new UriStatSummaryEntity();
        entity1.setUri("uri1");
        entity1.setVersion("version1");

        List<List<UriStatSummaryEntity>> groupBy = MapperUtils.groupByUriAndVersion(List.of(entity0, entity1), 2);
        assertEquals(2, groupBy.size());
    }

    @Test
    void groupByUriAndVersion_limit() {
        UriStatSummaryEntity entity0 = new UriStatSummaryEntity();
        entity0.setUri("uri0");
        entity0.setVersion("version0");

        UriStatSummaryEntity entity1 = new UriStatSummaryEntity();
        entity1.setUri("uri1");
        entity1.setVersion("version1");

        List<List<UriStatSummaryEntity>> groupBy = MapperUtils.groupByUriAndVersion(List.of(entity0, entity1), 1);
        assertEquals(1, groupBy.size());
    }


    @Test
    void groupByUriAndVersion_1() {
        UriStatSummaryEntity entity0 = new UriStatSummaryEntity();
        entity0.setUri("uri0");
        entity0.setVersion("version0");

        UriStatSummaryEntity entity1 = new UriStatSummaryEntity();
        entity1.setUri("uri0");
        entity1.setVersion("version0");

        List<List<UriStatSummaryEntity>> groupBy = MapperUtils.groupByUriAndVersion(List.of(entity0, entity1), 2);
        assertEquals(1, groupBy.size());
    }
}