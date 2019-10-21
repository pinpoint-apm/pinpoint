/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadCount {

    private final String agentId;
    private final List<Integer> activeThreadCountList;
    private final Status status;

    private AgentActiveThreadCount(Builder builder) {
        this.agentId = builder.agentId;
        this.activeThreadCountList = builder.activeThreadCountList;
        this.status = builder.status;
    }

    public String getAgentId() {
        return agentId;
    }

    public List<Integer> getActiveThreadCountList() {
        return activeThreadCountList;
    }

    public short getCode() {
        return status.code;
    }

    public String getCodeMessage() {
        return status.codeMessage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentActiveThreadCount{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", activeThreadCountList=").append(activeThreadCountList);
        sb.append(", code=").append(status.code);
        sb.append(", codeMessage='").append(status.codeMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }

    static class Builder {

        static final Status SUCCESS_STATUS = new Status((short) 0, "OK");
        static final Status UNKNOWN_STATUS = new Status((short) -1, "UNKNOWN");

        private String agentId;
        private List<Integer> activeThreadCountList;
        private Status status;

        Builder() {
        }

        Builder setAgentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        Builder setStatus(short code, String codeMessage) {
            this.status = new Status(code, codeMessage);
            return this;
        }

        Builder setActiveThreadCountList(List<Integer> activeThreadCountList) {
            this.activeThreadCountList = activeThreadCountList;
            return this;
        }

        Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        AgentActiveThreadCount build() {
            if (agentId == null) {
                throw new NullPointerException("agentId");
            }
            if (activeThreadCountList == null) {
                throw new NullPointerException("activeThreadCountList");
            }
            if (status == null) {
                throw new NullPointerException("status");
            }

            return new AgentActiveThreadCount(this);
        }

    }

    static class Status {

        private final short code;
        private final String codeMessage;

        Status(short code, String codeMessage) {
            this.code = code;
            this.codeMessage = codeMessage;
        }

    }

}
