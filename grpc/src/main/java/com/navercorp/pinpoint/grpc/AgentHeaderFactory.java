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

    public static final Metadata.Key<String> AGENT_ID_KEY = newStringKey("agentid");
    public static final Metadata.Key<String> APPLICATION_NAME_KEY = newStringKey("applicationname");
    public static final Metadata.Key<String> AGENT_START_TIME_KEY = newStringKey("starttime");

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
        final String agentId = headers.get(AGENT_ID_KEY);
        final String applicationName = headers.get(APPLICATION_NAME_KEY);
        final String agentStartTimeStr = headers.get(AGENT_START_TIME_KEY);
        Assert.requireNonNull(agentStartTimeStr, "agentStartTime must not be null");
        // check number format
        final long startTime = Long.parseLong(agentStartTimeStr);

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

        public Header(String agentId, String applicationName, long agentStartTime) {
            this.agentId = validateId(Assert.requireNonNull(agentId, "agentId must not be null"));
            this.applicationName = validateId(Assert.requireNonNull(applicationName, "applicationName must not be null"));
            this.agentStartTime = agentStartTime;
        }

        private String validateId(String id) {
            // TODO
            return id;
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


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Header{");
            sb.append("agentId='").append(agentId).append('\'');
            sb.append(", applicationName='").append(applicationName).append('\'');
            sb.append(", agentStartTime=").append(agentStartTime);
            sb.append('}');
            return sb.toString();
        }
    }
}
