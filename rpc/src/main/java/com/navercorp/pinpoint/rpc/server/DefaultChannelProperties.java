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

import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.util.MapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultChannelProperties implements ChannelProperties {
    private final String agentId;
    private final String applicationName;
    private final String hostIp;
    private final int pid;
    private final int serviceType;
    private final String hostName;
    private final long startTime;
    private final String agentVersion;
    private final int socketId;

    private final List<Integer> supportCommand;

    private final Map<String, Object> properties = new HashMap<String, Object>();

    public static ChannelProperties newChannelProperties(Map<Object, Object> properties) {
        if (com.navercorp.pinpoint.common.util.MapUtils.isEmpty(properties)) {
            return null;
        }

        final String agentId = MapUtils.getString(properties, HandshakePropertyType.AGENT_ID.getName());
        if (!IdValidateUtils.validateId(agentId)) {
            throw new IllegalArgumentException("Invalid agentId :" + agentId);
        }
        final String applicationName = MapUtils.getString(properties, HandshakePropertyType.APPLICATION_NAME.getName());
        if (!IdValidateUtils.validateId(applicationName)) {
            throw new IllegalArgumentException("Invalid applicationName :" + agentId);
        }
        final String hostName = MapUtils.getString(properties, HandshakePropertyType.HOSTNAME.getName());
        final String ip = MapUtils.getString(properties, HandshakePropertyType.IP.getName());
        final int pid = MapUtils.getInteger(properties, HandshakePropertyType.PID.getName(), -1);
        final int serviceType = MapUtils.getInteger(properties, HandshakePropertyType.SERVICE_TYPE.getName(), -1);
        final long startTime = MapUtils.getLong(properties, HandshakePropertyType.START_TIMESTAMP.getName(), -1L);
        final String version = MapUtils.getString(properties, HandshakePropertyType.VERSION.getName());
        final int socketId = MapUtils.getInteger(properties, "socketId", -1);
        List<Integer> supportCommandList = (List<Integer>) properties.get(HandshakePropertyType.SUPPORT_COMMAND_LIST.getName());
        if (supportCommandList == null) {
            supportCommandList = Collections.emptyList();
        }

        return new DefaultChannelProperties(agentId, applicationName, serviceType, version, hostName, ip, pid, startTime, socketId, supportCommandList);
    }

    public DefaultChannelProperties(String agentId, String applicationName, int serviceType, String agentVersion, String hostName, String hostIp, int pid, long startTime, int socketId, List<Integer> supportCommandList) {
        this.agentId = agentId;
        this.applicationName = applicationName;
        this.hostIp = hostIp;
        this.pid = pid;
        this.serviceType = serviceType;
        this.hostName = hostName;
        this.startTime = startTime;
        this.agentVersion = agentVersion;
        this.socketId = socketId;
        this.supportCommand = new ArrayList<Integer>(supportCommandList);
    }

    @Override
    public String getAgentId() {
        return this.agentId;
    }

    @Override
    public String getApplicationName() {
        return this.applicationName;
    }

    @Override
    public String getHostIp() {
        return this.hostIp;
    }

    @Override
    public int getPid() {
        return this.pid;
    }

    @Override
    public int getServiceType() {
        return this.serviceType;
    }

    @Override
    public String getHostName() {
        return this.hostName;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public String getAgentVersion() {
        return agentVersion;
    }


    @Override
    public int getSocketId() {
        return socketId;
    }

    @Override
    public List<Integer> getSupportCommand() {
        return supportCommand;
    }

    @Override
    public Object put(String key, Object value) {
        return properties.put(key, value);
    }

    @Override
    public Object get(String key) {
        return properties.get(key);
    }

    @Override
    public String toString() {
        return "DefaultChannelProperties{" +
                "agentId='" + agentId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", hostIp='" + hostIp + '\'' +
                ", pid=" + pid +
                ", serviceType=" + serviceType +
                ", hostName='" + hostName + '\'' +
                ", startTime=" + startTime +
                ", agentVersion='" + agentVersion + '\'' +
                ", socketId=" + socketId +
                ", supportCommand=" + supportCommand +
                ", properties=" + properties +
                '}';
    }
}
