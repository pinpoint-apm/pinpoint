package com.navercorp.pinpoint.web.cluster;

import org.junit.Assert;
import org.junit.Test;

public class ClusterIdTest {

    @Test
    public void newClusterId1() {
        ClusterId clusterId = ClusterId.newClusterId("/pinpoint/collector/test");

        Assert.assertEquals("/pinpoint/collector", clusterId.getParentPath());
        Assert.assertEquals("test", clusterId.getCollectorId());
        Assert.assertNull(clusterId.getApplicationName());
    }

    @Test
    public void newClusterByZKPath() {
        ClusterId clusterId = ClusterId.newClusterId("/pinpoint/collector", "HOST_NAME@1234$$appName");

        Assert.assertEquals("/pinpoint/collector", clusterId.getParentPath());
        Assert.assertEquals("HOST_NAME@1234", clusterId.getCollectorId());
        Assert.assertEquals("appName", clusterId.getApplicationName());
    }

    @Test
    public void newClusterByZKPath_noAppName() {
        ClusterId clusterId = ClusterId.newClusterId("/pinpoint/collector", "HOST_NAME@1234");

        Assert.assertEquals("/pinpoint/collector", clusterId.getParentPath());
        Assert.assertEquals("HOST_NAME@1234", clusterId.getCollectorId());
        Assert.assertNull(clusterId.getApplicationName());
    }
}