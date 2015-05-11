/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.cluster.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * @author Taejin Koo
 */
public final class ZookeeperUtils {

    // would be a good idea to move to commons-hbase (if implemented) in the future
    private ZookeeperUtils() {
    }

    public static boolean isConnectedEvent(WatchedEvent event) {
        KeeperState state = event.getState();
        EventType eventType = event.getType();

        return isConnectedEvent(state, eventType);
    }

    @SuppressWarnings("deprecation")
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
