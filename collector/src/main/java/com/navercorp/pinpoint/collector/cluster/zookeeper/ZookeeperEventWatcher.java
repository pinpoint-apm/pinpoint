package com.nhn.pinpoint.collector.cluster.zookeeper;

import org.apache.zookeeper.Watcher;

/**
 * @author koo.taejin
 */
public interface ZookeeperEventWatcher extends Watcher {

	boolean isConnected();

}
