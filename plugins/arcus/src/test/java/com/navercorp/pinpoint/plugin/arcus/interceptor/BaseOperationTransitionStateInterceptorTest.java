package com.navercorp.pinpoint.plugin.arcus.interceptor;

import org.junit.Assert;
import net.spy.memcached.ops.OperationState;
import org.junit.Test;

public class BaseOperationTransitionStateInterceptorTest {

    @Test
    public void testComplete() throws Exception {
        String complete = OperationState.COMPLETE.toString();
        Assert.assertEquals("COMPLETE", complete);
    }

    @Test
    public void existArcusTimeoutState() throws Exception {
        // 클래스가 강제 로딩되서 다른 test에 영향을 줄수 있음.
        if (!isArcusExist()) {
            // arcus만의 state체크를 위한 것이므로 없으면 패스한다.
            return;
        }
        // Arcus OperationState.timedout에 변경이 있는지 체크한다.
        OperationState[] values = OperationState.values();
        for (OperationState value : values) {
            if (value.toString().equals("TIMEDOUT")) {
                return;
            }
        }

        Assert.fail("OperationState.TIMEDOUT state not found");
    }

    private boolean isArcusExist() {
        try {
            Class.forName("net.spy.memcached.ArcusClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
