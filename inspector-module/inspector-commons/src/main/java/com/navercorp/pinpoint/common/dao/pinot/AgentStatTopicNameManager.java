/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.common.dao.pinot;

/**
 * @author minwoo-jung
 */
public class AgentStatTopicNameManager {

    public static AgentStatTopic getAgentStatTopic(int number) {
        for (AgentStatTopic agentStatTopic : AgentStatTopic.values()) {
            if (agentStatTopic.getNumber() == number) {
                return agentStatTopic;
            }
        }

        throw new IllegalArgumentException("Unknown AgentStatTopic number :" + number);
    }

    public enum AgentStatTopic {
        AGENT_STAT_00("inspector-stat-agent-00", 0),
        AGENT_STAT_01("inspector-stat-agent-01", 1),
        AGENT_STAT_02("inspector-stat-agent-02", 2),
        AGENT_STAT_03("inspector-stat-agent-03", 3),
        AGENT_STAT_04("inspector-stat-agent-04", 4),
        AGENT_STAT_05("inspector-stat-agent-05", 5),
        AGENT_STAT_06("inspector-stat-agent-06", 6),
        AGENT_STAT_07("inspector-stat-agent-07", 7),
        AGENT_STAT_08("inspector-stat-agent-08", 8),
        AGENT_STAT_09("inspector-stat-agent-09", 9),
        AGENT_STAT_10("inspector-stat-agent-10", 10),
        AGENT_STAT_11("inspector-stat-agent-11", 11),
        AGENT_STAT_12("inspector-stat-agent-12", 12),
        AGENT_STAT_13("inspector-stat-agent-13", 13),
        AGENT_STAT_14("inspector-stat-agent-14", 14),
        AGENT_STAT_15("inspector-stat-agent-15", 15);

        private final String topicName;
        private final int number;

        AgentStatTopic(String topicName, int number) {
            this.topicName = topicName;
            this.number = number;
        };

        public String getTopicName() {
            return topicName;
        }

        public int getNumber() {
            return number;
        }
    }

}
