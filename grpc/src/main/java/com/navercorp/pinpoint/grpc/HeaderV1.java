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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class HeaderV1 implements Header {

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

    public HeaderV1(String name, String agentId, String agentName, String applicationName,
                    int serviceType, long agentStartTime,
                    long socketId, List<Integer> supportCommandCodeList) {
        this(name, agentId, agentName, applicationName,
                serviceType, agentStartTime,
                socketId, supportCommandCodeList,
                DEFAULT_GRPC_BUILT_IN_RETRY, Collections.emptyMap());
    }

    public HeaderV1(String name,
                    String agentId, String agentName, String applicationName,
                    int serviceType,
                    long agentStartTime, long socketId,
                    List<Integer> supportCommandCodeList,
                    boolean grpcBuiltInRetry,
                    final Map<String, Object> properties) {
        this.name = Objects.requireNonNull(name, "name");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceType = serviceType;
        this.agentStartTime = agentStartTime;
        this.socketId = socketId;
        // allow null
        this.agentName = agentName;
        this.supportCommandCodeList = supportCommandCodeList;
        this.grpcBuiltInRetry = grpcBuiltInRetry;
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getServiceName() {
        return "DEFAULT";
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public long getSocketId() {
        return socketId;
    }

    @Override
    public int getServiceType() {
        return serviceType;
    }

    @Override
    public List<Integer> getSupportCommandCodeList() {
        return supportCommandCodeList;
    }

    @Override
    public boolean isGrpcBuiltInRetry() {
        return grpcBuiltInRetry;
    }

    @Override
    public Object get(String key) {
        return properties.get(key);
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String toString() {
        return "HeaderV1{" +
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeaderV1 header = (HeaderV1) o;

        if (agentStartTime != header.agentStartTime) return false;
        if (socketId != header.socketId) return false;
        if (serviceType != header.serviceType) return false;
        if (name != null ? !name.equals(header.name) : header.name != null) return false;
        if (agentId != null ? !agentId.equals(header.agentId) : header.agentId != null) return false;
        if (applicationName != null ? !applicationName.equals(header.applicationName) : header.applicationName != null)
            return false;
        if (grpcBuiltInRetry != header.grpcBuiltInRetry) return false;
        if (supportCommandCodeList != null ? !supportCommandCodeList.equals(header.supportCommandCodeList) : header.supportCommandCodeList != null)
            return false;
        return properties != null ? properties.equals(header.properties) : header.properties == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (agentId != null ? agentId.hashCode() : 0);
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        result = 31 * result + Long.hashCode(agentStartTime);
        result = 31 * result + Long.hashCode(socketId);
        result = 31 * result + serviceType;
        result = 31 * result + (supportCommandCodeList != null ? supportCommandCodeList.hashCode() : 0);
        result = 31 * result + (grpcBuiltInRetry ? 1 : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

}
