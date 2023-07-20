package com.navercorp.pinpoint.plugin.grpc;

import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class BuilderUtilsTest {
    @Test
    public void usePlainText() {
        ManagedChannelBuilder mock = mock(ManagedChannelBuilder.class);
        BuilderUtils.usePlainText(mock);
    }
}
