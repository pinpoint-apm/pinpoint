package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OutLinkColumnNameTest {

    @Test
    void testEquals() {

        ServiceType serviceType = ServiceType.TEST;
        short columnSlotNumber = 2;
        ColumnName name1 = OutLinkColumnName.histogram("callerApplicationName", serviceType, "callHost", columnSlotNumber);
        ColumnName name2 = OutLinkColumnName.histogram("callerApplicationName", serviceType, "callHost", columnSlotNumber);
        Assertions.assertEquals(name1, name2);
    }
}