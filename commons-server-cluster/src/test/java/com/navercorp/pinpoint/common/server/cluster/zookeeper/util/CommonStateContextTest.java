package com.navercorp.pinpoint.common.server.cluster.zookeeper.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonStateContextTest {

    @Test
    void getCurrentState() {
        CommonStateContext commonStateContext = new CommonStateContext();
        assertEquals(CommonState.NEW, commonStateContext.getCurrentState());
    }

    @Test
    void changeStateInitializing() {
        CommonStateContext commonStateContext = new CommonStateContext();
        assertTrue(commonStateContext.changeStateInitializing());
    }
}