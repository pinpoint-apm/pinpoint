package com.navercorp.pinpoint.common.server.bo.serializer.agent;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ApplicationNameRowKeyEncoderTest {

    @Test
    public void testGetTraceIndexRowKeyWhiteSpace() {
        String applicationName = "test test";
        long time = System.currentTimeMillis();
        check(applicationName, time);
    }

    @Test
    public void testGetTraceIndexRowKey1() {
        String applicationName = "test";
        long time = System.currentTimeMillis();
        check(applicationName, time);
    }

    @Test
    public void testGetTraceIndexRowKey2() {
        final String applicationName = "1".repeat(PinpointConstants.APPLICATION_NAME_MAX_LEN);

        long time = System.currentTimeMillis();
        check(applicationName, time);
    }

    @Test
    public void testGetTraceIndexRowKey3() {
        final String applicationName = "1".repeat(PinpointConstants.APPLICATION_NAME_MAX_LEN + 1);

        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            long time = System.currentTimeMillis();
            check(applicationName, time);
        });
    }

    private void check(String applicationName, long l1) {
        ApplicationNameRowKeyEncoder encoder = new ApplicationNameRowKeyEncoder();
        byte[] traceIndexRowKey = encoder.encodeRowKey(applicationName, l1);

        String agentId = BytesUtils.toString(traceIndexRowKey, 0, PinpointConstants.APPLICATION_NAME_MAX_LEN).trim();
        Assertions.assertEquals(applicationName, agentId);

        long time = toByteArray(Arrays.copyOfRange(traceIndexRowKey, PinpointConstants.APPLICATION_NAME_MAX_LEN, PinpointConstants.APPLICATION_NAME_MAX_LEN + 8));
        time = TimeUtils.recoveryTimeMillis(time);
        Assertions.assertEquals(time, l1);
    }

    private long toByteArray(byte[] bytes) {
        return ByteBuffer.allocate(8).put(bytes).getLong(0);
    }
}