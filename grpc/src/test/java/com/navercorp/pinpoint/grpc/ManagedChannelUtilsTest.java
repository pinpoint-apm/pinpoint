package com.navercorp.pinpoint.grpc;

import io.grpc.Channel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ManagedChannelUtilsTest {
    @Test
    public void testGetLogId() {
        Channel channel = mock(Channel.class);
        when(channel.toString()).thenReturn("ManagedChannelOrphanWrapper{delegate=ManagedChannelImpl{logId=1, target=127.0.0.1:9993}}");

        Assertions.assertEquals(1, ManagedChannelUtils.getLogId(channel));
    }
}