/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.rpc.server.PinpointServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Taejin Koo
 */
public class PinpointServerRepository {

    private final Map<String, Set<PinpointServer>> pinpointServerRepository = new HashMap<>();

    public boolean addAndIsKeyCreated(String key, PinpointServer pinpointServer) {
        synchronized (this) {
            final Set<PinpointServer> pinpointServerSet = pinpointServerRepository.get(key);
            if (pinpointServerSet != null) {
                pinpointServerSet.add(pinpointServer);

                return false;
            } else {
                Set<PinpointServer> newSet = new HashSet<>();
                newSet.add(pinpointServer);

                pinpointServerRepository.put(key, newSet);
                return true;
            }
        }
    }

    public boolean removeAndGetIsKeyRemoved(String key, PinpointServer pinpointServer) {
        synchronized (this) {
            final Set<PinpointServer> pinpointServerSet = pinpointServerRepository.get(key);
            if (pinpointServerSet != null) {
                pinpointServerSet.remove(pinpointServer);

                if (pinpointServerSet.isEmpty()) {
                    pinpointServerRepository.remove(key);
                    return true;
                }
            }
            return false;
        }
    }

    public void clear() {
        synchronized (this) {
            pinpointServerRepository.clear();
        }
    }

    public List<PinpointServer> getValues() {
        synchronized (this) {
            List<PinpointServer> pinpointServerList = new ArrayList<>(pinpointServerRepository.size());

            for (Set<PinpointServer> eachKeysValue : pinpointServerRepository.values()) {
                pinpointServerList.addAll(eachKeysValue);
            }

            return pinpointServerList;
        }
    }


}
