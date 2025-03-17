package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResponseColumnNameTest {

    @Test
    void testEquals() {

        short columnSlotNumber = 2;

        ResponseColumnName name1 = new ResponseColumnName("agentId", columnSlotNumber);
        ResponseColumnName name2 = new ResponseColumnName("agentId", columnSlotNumber);
        Assertions.assertEquals(name1, name2);
    }
}