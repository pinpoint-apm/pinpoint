package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.client.AsyncConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;

class RoundRobinSelectorTest {

    @Test
    void getConnection() {
        AsyncConnection con0 = mock(AsyncConnection.class);
        AsyncConnection con1 = mock(AsyncConnection.class);
        ConnectionSelector selector = new RoundRobinSelector(List.of(con0, con1));

        AsyncConnection r0 = selector.getConnection();
        AsyncConnection r1 = selector.getConnection();
        AsyncConnection r2 = selector.getConnection();
        Assertions.assertSame(con0, r0);
        Assertions.assertSame(con1, r1);
        Assertions.assertSame(con0, r2);
    }

    @Test
    void getConnection_overflow() {
        AsyncConnection con0 = mock(AsyncConnection.class);
        AsyncConnection con1 = mock(AsyncConnection.class);
        RoundRobinSelector selector = new RoundRobinSelector(List.of(con0, con1));
        selector.setModKey(Integer.MAX_VALUE);

        for (int i = 0; i < 10; i++) {
            selector.getConnection();
        }
    }

}