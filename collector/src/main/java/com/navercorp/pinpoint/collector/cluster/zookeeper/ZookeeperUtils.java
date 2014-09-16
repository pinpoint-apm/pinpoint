package com.nhn.pinpoint.collector.cluster.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * @author koo.taejin <kr14910>
 */
public final class ZookeeperUtils {

	// 나중에 commons-hbase 같은것이 생기면 그쪽에 포함하는 것도 방법일듯
	private ZookeeperUtils() {
	}

	public static boolean isConnectedEvent(WatchedEvent event) {
		KeeperState state = event.getState();
		EventType eventType = event.getType();

		return isConnectedEvent(state, eventType);
	}

	public static boolean isConnectedEvent(KeeperState state, EventType eventType) {
		if ((state == KeeperState.SyncConnected || state == KeeperState.NoSyncConnected) && eventType == EventType.None) {
			return true;
		} else {
			return false;
		}
	}

	
	public static boolean isDisconnectedEvent(WatchedEvent event) {
		KeeperState state = event.getState();
		EventType eventType = event.getType();

		return isDisconnectedEvent(state, eventType);
	}

	public static boolean isDisconnectedEvent(KeeperState state, EventType eventType) {
		if ((state == KeeperState.Disconnected || state == KeeperState.Expired) && eventType == eventType.None) {
			return true;
		} else {
			return false;
		}
	}
	
}
