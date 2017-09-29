/*
 * Copyright 2015 NAVER Corp.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;

/**
 * @author HyunGil Jeong
 */
@Deprecated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DurationalAgentEvent extends AgentEvent {

    public static final long UNKNOWN_TIMESTAMP = -1;

    @JsonProperty
    private long durationStartTimestamp = UNKNOWN_TIMESTAMP;

    @JsonProperty
    private long durationEndTimestamp = UNKNOWN_TIMESTAMP;

    public DurationalAgentEvent(AgentEventBo agentEventBo) {
        super(agentEventBo);
    }

    public long getDurationStartTimestamp() {
        return this.durationStartTimestamp;
    }

    public void setDurationStartTimestamp(long durationStartTimestamp) {
        this.durationStartTimestamp = durationStartTimestamp;
    }

    public long getDurationEndTimestamp() {
        return this.durationEndTimestamp;
    }

    public void setDurationEndTimestamp(long durationEndTimestamp) {
        this.durationEndTimestamp = durationEndTimestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DurationalAgentEvent{");
        sb.append("agentId=").append(super.getAgentId());
        sb.append(", eventTimestamp=").append(super.getEventTimestamp());
        sb.append(", eventTypeCode=").append(super.getEventTypeCode());
        sb.append(", eventTypeDesc=").append(super.getEventTypeDesc());
        sb.append(", hasEventMessage=").append(super.hasEventMessage());
        sb.append(", eventMessage=").append(super.getEventMessage());
        sb.append(", startTimestamp=").append(super.getStartTimestamp());
        sb.append(", durationStartTimestamp=").append(durationStartTimestamp);
        sb.append(", durationEndTimestamp=").append(durationEndTimestamp);
        sb.append('}');
        return sb.toString();
    }
}
