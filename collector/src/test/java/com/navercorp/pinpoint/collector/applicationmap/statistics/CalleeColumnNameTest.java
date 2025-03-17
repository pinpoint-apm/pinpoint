package com.navercorp.pinpoint.collector.applicationmap.statistics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CalleeColumnNameTest {

    @Test
    void testEquals() {

        short calleeServiceType = 1;
        short columnSlotNumber = 2;

        CalleeColumnName name1 = new CalleeColumnName("callerAgent", calleeServiceType, "calleeAgent", "host", columnSlotNumber);
        CalleeColumnName name2 = new CalleeColumnName("callerAgent", calleeServiceType, "calleeAgent", "host", columnSlotNumber);
        Assertions.assertEquals(name1, name2);

    }
}