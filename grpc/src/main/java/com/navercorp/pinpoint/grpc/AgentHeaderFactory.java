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
import io.grpc.Attributes;
import io.grpc.Metadata;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentHeaderFactory implements HeaderFactory<AgentHeaderFactory.Header> {

    public static final Metadata.Key<String> AGENT_ID_KEY = newStringKey("pinpoint-agentid");
    public static final Metadata.Key<String> APPLICATION_NAME_KEY = newStringKey("pinpoint-applicationname");
    public static final Metadata.Key<String> AGENT_START_TIME_KEY = newStringKey("pinpoint-agentstarttime");

    public static final Attributes.Key<String> KEY_REMOTE_ADDRESS = Attributes.Key.create("remoteAddress");
    public static final Attributes.Key<Integer> KEY_REMOTE_PORT = Attributes.Key.create("remotePort");
    public static final Attributes.Key<Integer> KEY_TRANSPORT_ID = Attributes.Key.create("transportId");

    private final Header header;

    private static Metadata.Key<String> newStringKey(String s) {
        return Metadata.Key.of(s, Metadata.ASCII_STRING_MARSHALLER);
    }

    public AgentHeaderFactory() {
        this.header = null;
    }

    public AgentHeaderFactory(Header header) {
        this.header = Assert.requireNonNull(header, "header must not be null");
    }

    public Header extract(Metadata headers) {
        return extract(headers, null);
    }

    public Header extract(Metadata headers, Attributes attributes) {
        final String agentId = headers.get(AGENT_ID_KEY);
        final String applicationName = headers.get(APPLICATION_NAME_KEY);
        final String agentStartTimeStr = headers.get(AGENT_START_TIME_KEY);
        Assert.requireNonNull(agentStartTimeStr, "agentStartTime must not be null");
        // check number format
        final long startTime = Long.parseLong(agentStartTimeStr);

        if (attributes != null) {
            final String remoteAddress = attributes.get(KEY_REMOTE_ADDRESS);
            final int remotePort = attributes.get(KEY_REMOTE_PORT);
            final int transportId = attributes.get(KEY_TRANSPORT_ID);
            return new Header(agentId, applicationName, startTime, remoteAddress, remotePort, transportId);
        }
        return new Header(agentId, applicationName, startTime);
    }


    public Metadata newHeader() {
        Metadata headers = new Metadata();
        headers.put(AGENT_ID_KEY, header.getAgentId());
        headers.put(APPLICATION_NAME_KEY, header.getApplicationName());
        headers.put(AGENT_START_TIME_KEY, Long.toString(header.getAgentStartTime()));
        return headers;
    }

    public static class Header {
        private final String agentId;
        private final String applicationName;
        private final long agentStartTime;
        private String remoteAddress;
        private int remotePort;
        private int transportId;

        public Header(String agentId, String applicationName, long agentStartTime) {
            this(agentId, applicationName, agentStartTime, "", 0, 0);
        }

        public Header(String agentId, String applicationName, long agentStartTime, String remoteAddress, int remotePort, int transportId) {
            this.agentId = validateId(Assert.requireNonNull(agentId, "agentId must not be null"));
            this.applicationName = validateId(Assert.requireNonNull(applicationName, "applicationName must not be null"));
            this.agentStartTime = agentStartTime;
            this.remoteAddress = remoteAddress;
            this.remotePort = remotePort;
            this.transportId = transportId;
        }

        private String validateId(String agentId) {
            // TODO
            return agentId;
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

        public String getRemoteAddress() {
            return remoteAddress;
        }

        public int getRemotePort() {
            return remotePort;
        }

        public int getTransportId() {
            return transportId;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Header{");
            sb.append("agentId='").append(agentId).append('\'');
            sb.append(", applicationName='").append(applicationName).append('\'');
            sb.append(", agentStartTime=").append(agentStartTime);
            sb.append(", remoteAddress='").append(remoteAddress).append('\'');
            sb.append(", remotePort=").append(remotePort);
            sb.append(", transportId=").append(transportId);
            sb.append('}');
            return sb.toString();
        }
    }
}
