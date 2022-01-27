package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import org.apache.zookeeper.ZooKeeper;

public final class ZKUtils {
    private ZKUtils() {
    }

    public static void closeQuietly(ZooKeeper zookeeper) {
        if (zookeeper != null) {
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
