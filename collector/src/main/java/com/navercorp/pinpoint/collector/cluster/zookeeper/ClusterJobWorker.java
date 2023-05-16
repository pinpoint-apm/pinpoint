package com.navercorp.pinpoint.collector.cluster.zookeeper;

import java.util.List;

public interface ClusterJobWorker<K> {
    void start();

    void stop();

    void addPinpointServer(K key);

    List<String> getClusterList();

    void removePinpointServer(K key);

    void clear();
}
