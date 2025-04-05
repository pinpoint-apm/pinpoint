package com.navercorp.pinpoint.inspector.collector.service;

import com.navercorp.pinpoint.common.server.bo.stat.StatDataPoint;
import com.navercorp.pinpoint.inspector.collector.dao.pinot.PinotTypeMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;



class PinotMappersTest {
    @Test
    void testGetMapper() {
        PinotMappers pinotMappers = new PinotMappers();
        List<PinotTypeMapper<StatDataPoint>> mappers = pinotMappers.getMapper();
        Assertions.assertFalse(mappers.isEmpty());
    }
}