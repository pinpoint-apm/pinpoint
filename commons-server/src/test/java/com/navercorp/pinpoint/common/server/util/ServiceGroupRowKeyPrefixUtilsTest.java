package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceGroupRowKeyPrefixUtilsTest {

    @Test
    public void createRowKeyTest() {
        ServiceUid serviceUid = ServiceUid.TEST;
        String applicationName = "appName";
        int serviceTypeCode = 1000;
        byte[] rowKey1 = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid);
        byte[] rowKey2 = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName);
        byte[] rowKey3 = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);

        Assertions.assertThat(rowKey1).hasSize(Integer.BYTES);
        Assertions.assertThat(rowKey2).hasSize(Integer.BYTES + PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);

        Assertions.assertThat(rowKey2).startsWith(rowKey1);
        Assertions.assertThat(rowKey3).startsWith(rowKey2);
    }
}
