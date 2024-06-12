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

package com.navercorp.pinpoint.grpc;

import com.navercorp.pinpoint.common.id.AgentId;
import io.grpc.Metadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class Header {

    public static final Metadata.Key<String> AGENT_ID_KEY = newStringKey("agentid");
    public static final Metadata.Key<String> AGENT_NAME_KEY = newStringKey("agentname");
    public static final Metadata.Key<String> APPLICATION_NAME_KEY = newStringKey("applicationname");
    public static final Metadata.Key<String> SERVICE_NAME_KEY = newStringKey("servicename");
    public static final Metadata.Key<String> AGENT_START_TIME_KEY = newStringKey("starttime");

    // optional header
    public static final Metadata.Key<String> SOCKET_ID = newStringKey("socketid");
    public static final Metadata.Key<String> SERVICE_TYPE_KEY = newStringKey("servicetype");
    public static final Metadata.Key<String> SUPPORT_COMMAND_CODE = newStringKey("supportCommandCode");

    public static final String SUPPORT_COMMAND_CODE_DELIMITER = ";";

    private static Metadata.Key<String> newStringKey(String s) {
        return Metadata.Key.of(s, Metadata.ASCII_STRING_MARSHALLER);
    }

    public static final long SOCKET_ID_NOT_EXIST = -1;

    public static final List<Integer> SUPPORT_COMMAND_CODE_LIST_NOT_EXIST = null;
    public static final List<Integer> SUPPORT_COMMAND_CODE_LIST_PARSE_ERROR = Collections.emptyList();

    private final String name;
    private final AgentId agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;
    private final long agentStartTime;
    private final long socketId;
    private final int serviceType;
    private final List<Integer> supportCommandCodeList;
    private final Map<String, Object> properties;

    public Header(String name, AgentId agentId, String agentName, String applicationName, String serviceName,
                  int serviceType, long agentStartTime,
                  long socketId, List<Integer> supportCommandCodeList) {
        this(name, agentId, agentName, applicationName, serviceName,
                serviceType, agentStartTime,
                socketId, supportCommandCodeList, Collections.emptyMap());
    }

    public Header(String name,
                  AgentId agentId, String agentName, String applicationName, String serviceName,
                  int serviceType,
                  long agentStartTime, long socketId,
                  List<Integer> supportCommandCodeList,
                  final Map<String, Object> properties) {
        this.name = Objects.requireNonNull(name, "name");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.serviceType = serviceType;
        this.agentStartTime = agentStartTime;
        this.socketId = socketId;
        // allow null
        this.agentName = agentName;
        this.supportCommandCodeList = supportCommandCodeList;
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public AgentId getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getSocketId() {
        return socketId;
    }

    public int getServiceType() {
        return serviceType;
    }

    public List<Integer> getSupportCommandCodeList() {
        return supportCommandCodeList;
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String toString() {
        return "Header{" +
                "name='" + name + '\'' +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", socketId=" + socketId +
                ", serviceType=" + serviceType +
                ", supportCommandCodeList=" + supportCommandCodeList +
                ", properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        if (agentStartTime != header.agentStartTime) return false;
        if (socketId != header.socketId) return false;
        if (serviceType != header.serviceType) return false;
        if (name != null ? !name.equals(header.name) : header.name != null) return false;
        if (agentId != null ? !agentId.equals(header.agentId) : header.agentId != null) return false;
        if (applicationName != null ? !applicationName.equals(header.applicationName) : header.applicationName != null)
            return false;
        if (serviceName != null ? !serviceName.equals(header.serviceName) : header.serviceName != null)
            return false;
        if (supportCommandCodeList != null ? !supportCommandCodeList.equals(header.supportCommandCodeList) : header.supportCommandCodeList != null)
            return false;
        return properties != null ? properties.equals(header.properties) : header.properties == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (agentId != null ? agentId.hashCode() : 0);
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
        result = 31 * result + (int) (socketId ^ (socketId >>> 32));
        result = 31 * result + serviceType;
        result = 31 * result + (supportCommandCodeList != null ? supportCommandCodeList.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

}
