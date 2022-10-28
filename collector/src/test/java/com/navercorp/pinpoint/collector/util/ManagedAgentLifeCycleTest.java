package com.navercorp.pinpoint.collector.util;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;


public class ManagedAgentLifeCycleTest {

    @Test
    public void getEventCounter() {
        Assertions.assertEquals(0, ManagedAgentLifeCycle.RUNNING.getEventCounter());
    }
}