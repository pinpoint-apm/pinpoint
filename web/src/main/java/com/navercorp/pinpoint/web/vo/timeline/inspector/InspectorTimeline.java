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

package com.navercorp.pinpoint.web.vo.timeline.inspector;

/**
 * @author HyunGil Jeong
 */
public class InspectorTimeline {

    private final AgentStatusTimeline agentStatusTimeline;
    private final AgentEventTimeline agentEventTimeline;

    public InspectorTimeline(AgentStatusTimeline agentStatusTimeline, AgentEventTimeline agentEventTimeline) {
        this.agentStatusTimeline = agentStatusTimeline;
        this.agentEventTimeline = agentEventTimeline;
    }

    public AgentStatusTimeline getAgentStatusTimeline() {
        return agentStatusTimeline;
    }

    public AgentEventTimeline getAgentEventTimeline() {
        return agentEventTimeline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InspectorTimeline that = (InspectorTimeline) o;

        if (agentStatusTimeline != null ? !agentStatusTimeline.equals(that.agentStatusTimeline) : that.agentStatusTimeline != null)
            return false;
        return agentEventTimeline != null ? agentEventTimeline.equals(that.agentEventTimeline) : that.agentEventTimeline == null;
    }

    @Override
    public int hashCode() {
        int result = agentStatusTimeline != null ? agentStatusTimeline.hashCode() : 0;
        result = 31 * result + (agentEventTimeline != null ? agentEventTimeline.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InspectorTimeline{");
        sb.append("agentStatusTimeline=").append(agentStatusTimeline);
        sb.append(", agentEventTimeline=").append(agentEventTimeline);
        sb.append('}');
        return sb.toString();
    }
}
