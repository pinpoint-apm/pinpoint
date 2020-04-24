/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.rpc.client.HandshakerFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.util.MapUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ChannelPropertiesFactory {
    private final String[] customKey;


    public ChannelPropertiesFactory() {
        this.customKey = new String[0];
    }

    public ChannelPropertiesFactory(String customKeyList) {
        this.customKey = copy(customKeyList);
    }

    private String[] copy(String customKey) {
        if (StringUtils.isEmpty(customKey)) {
            return new String[0];
        }
        List<String> keyList = StringUtils.tokenizeToStringList(customKey, ",");
        return keyList.toArray(new String[0]);
    }

    public ChannelProperties newChannelProperties(Map<Object, Object> properties) {
        if (com.navercorp.pinpoint.common.util.MapUtils.isEmpty(properties)) {
            return null;
        }

        final String agentId = MapUtils.getString(properties, HandshakePropertyType.AGENT_ID.getName());
        if (!IdValidateUtils.validateId(agentId)) {
            throw new IllegalArgumentException("Invalid agentId :" + agentId);
        }
        final String applicationName = MapUtils.getString(properties, HandshakePropertyType.APPLICATION_NAME.getName());
        if (!IdValidateUtils.validateId(applicationName)) {
            throw new IllegalArgumentException("Invalid applicationName :" + applicationName);
        }
        final String hostName = MapUtils.getString(properties, HandshakePropertyType.HOSTNAME.getName());
        final String ip = MapUtils.getString(properties, HandshakePropertyType.IP.getName());
        final int pid = MapUtils.getInteger(properties, HandshakePropertyType.PID.getName(), -1);
        final int serviceType = MapUtils.getInteger(properties, HandshakePropertyType.SERVICE_TYPE.getName(), -1);
        final long startTime = MapUtils.getLong(properties, HandshakePropertyType.START_TIMESTAMP.getName(), -1L);
        final String version = MapUtils.getString(properties, HandshakePropertyType.VERSION.getName());
        final int socketId = MapUtils.getInteger(properties, HandshakerFactory.SOCKET_ID, -1);
        List<Integer> supportCommandList = (List<Integer>) properties.get(HandshakePropertyType.SUPPORT_COMMAND_LIST.getName());
        if (supportCommandList == null) {
            supportCommandList = Collections.emptyList();
        }

        final Map<Object, Object> customProperty = Collections.unmodifiableMap(copyCustomProperty(properties));
        return new DefaultChannelProperties(agentId, applicationName, serviceType, version, hostName, ip, pid, startTime, socketId, supportCommandList, customProperty);
    }

    private Map<Object, Object> copyCustomProperty(Map<Object, Object> properties) {
        if (ArrayUtils.isEmpty(customKey)) {
            return Collections.emptyMap();
        }
        final Map<Object, Object> copy = new HashMap<Object, Object>();
        for (Object key : customKey) {
            final Object value = properties.get(key);
            if (value != null) {
                copy.put(key, value);
            }
        }
        return copy;
    }
}
