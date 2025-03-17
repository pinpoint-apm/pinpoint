package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CallerColumnNameTest {

    @Test
    void testEquals() {

        short serviceType = 1;
        short columnSlotNumber = 2;
        CallerColumnName name1 = new CallerColumnName(serviceType, "callerApplicationName",  "callHost", columnSlotNumber);
        CallerColumnName name2 = new CallerColumnName(serviceType, "callerApplicationName",  "callHost", columnSlotNumber);
        Assertions.assertEquals(name1, name2);
    }
}