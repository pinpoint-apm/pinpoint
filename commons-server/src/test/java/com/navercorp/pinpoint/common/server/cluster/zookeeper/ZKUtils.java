package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import org.apache.zookeeper.ZooKeeper;

public final class ZKUtils {
    private ZKUtils() {
    }

    public static void close(ZooKeeper zookeeper) throws InterruptedException {
        if (zookeeper != null) {
            zookeeper.close();
        }
    }

    public static void close(ZookeeperClient zookeeperClient) {
        if (zookeeperClient != null) {
            zookeeperClient.close();
        }
    }
}
