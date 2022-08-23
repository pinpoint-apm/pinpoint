package com.navercorp.pinpoint.grpc.server.flowcontrol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultIdleTimeoutTest {

    @Test
    public void isExpired_invalid_parameter() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new DefaultIdleTimeout(-1); 
        });
    }

    @Test
    public void isExpired_init_state() {
        IdleTimeout idleTimeout = new DefaultIdleTimeout(5000);
        Assertions.assertFalse(idleTimeout.isExpired());
        Assertions.assertFalse(idleTimeout.isExpired());
    }

    @Test
    public void isExpired_expired() {
        Clock clock = mock(Clock.class);
        when(clock.millis()).thenReturn(1L);

        IdleTimeout idleTimeout = new DefaultIdleTimeout(0, clock);

        Assertions.assertTrue(idleTimeout.isExpired());
        Assertions.assertTrue(idleTimeout.isExpired());
    }

    @Test
    public void isExpired_update() {
        final Clock clock = mock(Clock.class);
        when(clock.millis()).thenReturn(0L);

        IdleTimeout idleTimeout = new DefaultIdleTimeout(2, clock);
        Assertions.assertFalse(idleTimeout.isExpired());

        when(clock.millis()).thenReturn(5L);
        Assertions.assertTrue(idleTimeout.isExpired());

        when(clock.millis()).thenReturn(0L);
        Assertions.assertTrue(idleTimeout.isExpired());
    }

}