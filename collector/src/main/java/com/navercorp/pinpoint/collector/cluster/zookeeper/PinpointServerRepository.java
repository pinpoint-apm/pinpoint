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

import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @Author Taejin Koo
 */
public class PinpointServerRepository {

    private final Map<String, Set<PinpointServer>> pinpointServerRepository = new HashMap<>();

    public boolean addAndIsKeyCreated(String key, PinpointServer pinpointServer) {
        synchronized (this) {
            boolean isContains = pinpointServerRepository.containsKey(key);
            if (isContains) {
                Set<PinpointServer> pinpointServerSet = pinpointServerRepository.get(key);
                pinpointServerSet.add(pinpointServer);

                return false;
            } else {
                Set<PinpointServer> pinpointServerSet = new HashSet<>();
                pinpointServerSet.add(pinpointServer);

                pinpointServerRepository.put(key, pinpointServerSet);
                return true;
            }
        }
    }

    public boolean removeAndGetIsKeyRemoved(String key, PinpointServer pinpointServer) {
        synchronized (this) {
            boolean isContains = pinpointServerRepository.containsKey(key);
            if (isContains) {
                Set<PinpointServer> pinpointServerSet = pinpointServerRepository.get(key);
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
        List<PinpointServer> pinpointServerList = new ArrayList<>(pinpointServerRepository.size());

        for (Set<PinpointServer> eachKeysValue : pinpointServerRepository.values()) {
            pinpointServerList.addAll(eachKeysValue);
        }

        return pinpointServerList;
    }

    private String getKey(PinpointServer pinpointServer) {
        Map<Object, Object> properties = pinpointServer.getChannelProperties();
        final String applicationName = MapUtils.getString(properties, HandshakePropertyType.APPLICATION_NAME.getName());
        final String agentId = MapUtils.getString(properties, HandshakePropertyType.AGENT_ID.getName());
        final Long startTimeStamp = MapUtils.getLong(properties, HandshakePropertyType.START_TIMESTAMP.getName());

        if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStamp == null || startTimeStamp <= 0) {
            return StringUtils.EMPTY;
        }

        return applicationName + ":" + agentId + ":" + startTimeStamp;
    }

}
