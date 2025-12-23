package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceGroupRowKeyPrefixUtilsTest {

    @Test
    public void createRowKeyTest() {
        byte[] rowKey1 = ServiceGroupRowKeyPrefixUtils.createRowKey(ServiceUid.TEST, "appName", 1000);
        byte[] rowKey2 = ServiceGroupRowKeyPrefixUtils.createRowKey(ServiceUid.TEST, "appName");
        byte[] rowKey3 = ServiceGroupRowKeyPrefixUtils.createRowKey(ServiceUid.TEST);

        Assertions.assertThat(rowKey1).startsWith(rowKey2);
        Assertions.assertThat(rowKey2).startsWith(rowKey3);
    }

    @Test
    public void expectedBufferSizeTest() {
        for (int i = 1; i <= 24; i++) {
            String agentId = "_".repeat(i);
            byte[] rowKey = ServiceGroupRowKeyPrefixUtils.createRowKey(ServiceUid.DEFAULT, "appName", 1000, agentId);
            Assertions.assertThat(rowKey).hasSize(4 + PinpointConstants.APPLICATION_NAME_MAX_LEN_V3 + 4 + 1 + i);
        }
    }
}
