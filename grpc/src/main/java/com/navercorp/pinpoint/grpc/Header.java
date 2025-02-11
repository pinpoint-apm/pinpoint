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
    public static final Metadata.Key<String> AGENT_START_TIME_KEY = newStringKey("starttime");

    // optional header
    public static final Metadata.Key<String> SOCKET_ID = newStringKey("socketid");
    public static final Metadata.Key<String> SERVICE_TYPE_KEY = newStringKey("servicetype");
    public static final Metadata.Key<String> SUPPORT_COMMAND_CODE = newStringKey("supportCommandCode");
    public static final Metadata.Key<String> GRPC_BUILT_IN_RETRY = newStringKey("grpc.built-in.retry");

    public static final String SUPPORT_COMMAND_CODE_DELIMITER = ";";

    private static Metadata.Key<String> newStringKey(String s) {
        return Metadata.Key.of(s, Metadata.ASCII_STRING_MARSHALLER);
    }

    public static final long SOCKET_ID_NOT_EXIST = -1;

    public static final List<Integer> SUPPORT_COMMAND_CODE_LIST_NOT_EXIST = null;
    public static final List<Integer> SUPPORT_COMMAND_CODE_LIST_PARSE_ERROR = Collections.emptyList();
    public static final boolean DEFAULT_GRPC_BUILT_IN_RETRY = false;

    private final String name;
    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final long agentStartTime;
    private final long socketId;
    private final int serviceType;
    private final List<Integer> supportCommandCodeList;
    private final boolean grpcBuiltInRetry;
    private final Map<String, Object> properties;

    public Header(String name, String agentId, String agentName, String applicationName,
                  int serviceType, long agentStartTime,
                  long socketId,
                  List<Integer> supportCommandCodeList) {
        this(name, agentId, agentName, applicationName,
                serviceType, agentStartTime,
                socketId, supportCommandCodeList,
                DEFAULT_GRPC_BUILT_IN_RETRY, Collections.emptyMap());
    }

    public Header(String name,
                  String agentId, String agentName, String applicationName,
                  int serviceType,
                  long agentStartTime,
                  long socketId,
                  List<Integer> supportCommandCodeList,
                  boolean grpcBuiltInRetry,
                  final Map<String, Object> properties) {
        this.name = Objects.requireNonNull(name, "name");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        // allow null
        this.agentName = agentName;
        this.serviceType = serviceType;
        this.agentStartTime = agentStartTime;
        this.socketId = socketId;
        this.supportCommandCodeList = supportCommandCodeList;
        this.grpcBuiltInRetry = grpcBuiltInRetry;
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getApplicationName() {
        return applicationName;
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

    public boolean isGrpcBuiltInRetry() {
        return grpcBuiltInRetry;
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
                ", agentStartTime=" + agentStartTime +
                ", socketId=" + socketId +
                ", serviceType=" + serviceType +
                ", supportCommandCodeList=" + supportCommandCodeList +
                ", grpcBuiltInRetry='" + grpcBuiltInRetry + '\'' +
                ", properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;
        return agentStartTime == header.agentStartTime && socketId == header.socketId && serviceType == header.serviceType && grpcBuiltInRetry == header.grpcBuiltInRetry && Objects.equals(name, header.name) && Objects.equals(agentId, header.agentId) && Objects.equals(agentName, header.agentName) && Objects.equals(applicationName, header.applicationName) && Objects.equals(supportCommandCodeList, header.supportCommandCodeList) && Objects.equals(properties, header.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(agentId);
        result = 31 * result + Objects.hashCode(agentName);
        result = 31 * result + Objects.hashCode(applicationName);
        result = 31 * result + Long.hashCode(agentStartTime);
        result = 31 * result + Long.hashCode(socketId);
        result = 31 * result + serviceType;
        result = 31 * result + Objects.hashCode(supportCommandCodeList);
        result = 31 * result + Boolean.hashCode(grpcBuiltInRetry);
        result = 31 * result + Objects.hashCode(properties);
        return result;
    }
}
