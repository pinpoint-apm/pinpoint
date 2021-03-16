package com.navercorp.pinpoint.grpc.server.flowcontrol;

import com.navercorp.pinpoint.common.util.Clock;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultIdleTimeoutTest {

    @Test(expected = IllegalArgumentException.class)
    public void isExpired_invalid_parameter() {
        new DefaultIdleTimeout(-1);
    }

    @Test
    public void isExpired_init_state() {
        IdleTimeout idleTimeout = new DefaultIdleTimeout(5000);
        Assert.assertFalse(idleTimeout.isExpired());
        Assert.assertFalse(idleTimeout.isExpired());
    }

    @Test
    public void isExpired_expired() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(1L);

        IdleTimeout idleTimeout = new DefaultIdleTimeout(0, clock);

        Assert.assertTrue(idleTimeout.isExpired());
        Assert.assertTrue(idleTimeout.isExpired());
    }

    @Test
    public void isExpired_update() {
        final Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(0L);


        IdleTimeout idleTimeout = new DefaultIdleTimeout(2, clock);
        Assert.assertFalse(idleTimeout.isExpired());

        when(clock.getTime()).thenReturn(5L);
        Assert.assertTrue(idleTimeout.isExpired());

        when(clock.getTime()).thenReturn(0L);
        Assert.assertTrue(idleTimeout.isExpired());
    }

}