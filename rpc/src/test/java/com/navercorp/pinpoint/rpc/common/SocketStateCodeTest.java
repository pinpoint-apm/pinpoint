package com.navercorp.pinpoint.rpc.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SocketStateCodeTest {

    @Test
    public void isBeforeConnected() {
        SocketStateCode code = SocketStateCode.BEING_CONNECT;
        Assertions.assertTrue(code.isBeforeConnected());
    }

    @Test
    public void canChangeState() {
        SocketStateCode code = SocketStateCode.BEING_CONNECT;
        Assertions.assertTrue(code.canChangeState(SocketStateCode.CONNECTED));
    }
}