package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import org.apache.zookeeper.ZooKeeper;

import java.io.Closeable;
import java.io.IOException;

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

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

}
