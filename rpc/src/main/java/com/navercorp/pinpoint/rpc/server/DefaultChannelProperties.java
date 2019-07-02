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


import java.util.ArrayList;
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

    private final Map<Object, Object> properties;


    public DefaultChannelProperties(String agentId, String applicationName, int serviceType, String agentVersion, String hostName, String hostIp, int pid, long startTime, int socketId, List<Integer> supportCommandList, Map<Object, Object> customProperty) {
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
        this.properties = new HashMap<Object, Object>(customProperty);
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
    public Object get(Object key) {
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
