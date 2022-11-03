package com.navercorp.pinpoint.collector.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ManagedAgentLifeCycleTest {

    @Test
    public void getEventCounter() {
        Assertions.assertEquals(0, ManagedAgentLifeCycle.RUNNING.getEventCounter());
    }
}