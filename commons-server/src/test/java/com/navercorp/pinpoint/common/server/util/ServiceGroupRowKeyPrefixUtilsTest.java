package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceGroupRowKeyPrefixUtilsTest {

    @Test
    public void createRowKeyTest() {
        int serviceUid = ServiceUid.TEST.getUid();
        String applicationName = "appName";
        int serviceTypeCode = 1000;
        byte[] rowKey1 = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid);
        byte[] rowKey2 = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName);
        byte[] rowKey3 = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);

        Assertions.assertThat(rowKey1).hasSize(Integer.BYTES);
        Assertions.assertThat(rowKey2).hasSize(Integer.BYTES + applicationName.length() + 1);
        Assertions.assertThat(rowKey3).hasSize(Integer.BYTES + applicationName.length() + 1 + Integer.BYTES);

        Assertions.assertThat(rowKey2).startsWith(rowKey1);
        Assertions.assertThat(rowKey3).startsWith(rowKey2);
    }
}
