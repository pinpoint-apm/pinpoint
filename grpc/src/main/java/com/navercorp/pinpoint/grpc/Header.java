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

import com.navercorp.pinpoint.common.util.Assert;

import io.grpc.Metadata;

import java.util.Collections;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Header {

    public static final Metadata.Key<String> AGENT_ID_KEY = newStringKey("agentid");
    public static final Metadata.Key<String> APPLICATION_NAME_KEY = newStringKey("applicationname");
    public static final Metadata.Key<String> AGENT_START_TIME_KEY = newStringKey("starttime");

    // optional header
    public static final Metadata.Key<String> SOCKET_ID = newStringKey("socketid");

    private static Metadata.Key<String> newStringKey(String s) {
        return Metadata.Key.of(s, Metadata.ASCII_STRING_MARSHALLER);
    }

    public static final long SOCKET_ID_NOT_EXIST = -1;


    private final String agentId;
    private final String applicationName;
    private final long agentStartTime;

    private final long socketId;

    private final Map<String, Object> properties;

    public Header(String agentId, String applicationName, long agentStartTime, long socketId) {
        this(agentId, applicationName, agentStartTime, socketId, Collections.<String, Object>emptyMap());
    }

    public Header(String agentId, String applicationName, long agentStartTime, long socketId, final Map<String, Object> properties) {
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName");
        this.agentStartTime = agentStartTime;
        this.socketId = socketId;

        this.properties = Assert.requireNonNull(properties, "properties");
    }

    public String getAgentId() {
        return agentId;
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

    public Object get(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String toString() {
        return "Header{" +
                "agentId='" + agentId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", socketId=" + socketId +
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
        if (agentId != null ? !agentId.equals(header.agentId) : header.agentId != null) return false;
        if (applicationName != null ? !applicationName.equals(header.applicationName) : header.applicationName != null) return false;
        return properties != null ? properties.equals(header.properties) : header.properties == null;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
        result = 31 * result + (int) (socketId ^ (socketId >>> 32));
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

}
