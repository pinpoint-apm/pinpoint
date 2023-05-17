/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.dto;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ATCSupply {

    private String applicationName;
    private String agentId;
    private long startTimestamp;
    private String collectorId;
    private List<Integer> values;
    private Message message;

    @SuppressWarnings("unused")
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @SuppressWarnings("unused")
    public String getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(String collectorId) {
        this.collectorId = collectorId;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ATCSupply atcSupply = (ATCSupply) o;
        return startTimestamp == atcSupply.startTimestamp && Objects.equals(applicationName, atcSupply.applicationName) && Objects.equals(agentId, atcSupply.agentId) && Objects.equals(collectorId, atcSupply.collectorId) && Objects.equals(values, atcSupply.values) && Objects.equals(message, atcSupply.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, agentId, startTimestamp, collectorId, values, message);
    }

    @Override
    public String toString() {
        return "ATCSupply{" +
                "applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", collectorId='" + collectorId + '\'' +
                ", values=" + values +
                ", message='" + message + '\'' +
                '}';
    }

    public enum Message {
        OK("OK"),
        CONNECTED("CONNECTED"),
        WEB_ERROR("WEB ERROR");

        private final String message;

        Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
