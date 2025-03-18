package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InLinkColumnNameTest {

    @Test
    void testEquals() {

        ServiceType calleeServiceType = ServiceType.TEST;
        short columnSlotNumber = 2;

        ColumnName name1 = InLinkColumnName.histogram("callerAgent", calleeServiceType, "calleeAgent", "host", columnSlotNumber);
        ColumnName name2 = InLinkColumnName.histogram("callerAgent", calleeServiceType, "calleeAgent", "host", columnSlotNumber);
        Assertions.assertEquals(name1, name2);

    }
}