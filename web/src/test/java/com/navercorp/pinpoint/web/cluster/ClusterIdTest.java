package com.navercorp.pinpoint.web.cluster;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClusterIdTest {

    @Test
    public void newClusterId1() {
        ClusterId clusterId = ClusterId.newClusterId("/pinpoint/collector/test");

        Assertions.assertEquals("/pinpoint/collector", clusterId.getParentPath());
        Assertions.assertEquals("test", clusterId.getCollectorId());
        Assertions.assertNull(clusterId.getApplicationName());
    }

    @Test
    public void newClusterByZKPath() {
        ClusterId clusterId = ClusterId.newClusterId("/pinpoint/collector", "HOST_NAME@1234$$appName");

        Assertions.assertEquals("/pinpoint/collector", clusterId.getParentPath());
        Assertions.assertEquals("HOST_NAME@1234", clusterId.getCollectorId());
        Assertions.assertEquals("appName", clusterId.getApplicationName());
    }

    @Test
    public void newClusterByZKPath_noAppName() {
        ClusterId clusterId = ClusterId.newClusterId("/pinpoint/collector", "HOST_NAME@1234");

        Assertions.assertEquals("/pinpoint/collector", clusterId.getParentPath());
        Assertions.assertEquals("HOST_NAME@1234", clusterId.getCollectorId());
        Assertions.assertNull(clusterId.getApplicationName());
    }
}