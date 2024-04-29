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

import org.apache.kafka.common.utils.Utils;

/**
 * @author minwoo-jung
 */
public class AgentStatTopicAndTableNameManager {

    public static String getAgentStatTopicName(String applicationName, int agentStatTopicCount) {
        int hashValue = getHashValue(applicationName, agentStatTopicCount);

        for (AgentStatTopicAndTable agentStatTopic : AgentStatTopicAndTable.values()) {
            if (agentStatTopic.getNumber() == hashValue) {
                return agentStatTopic.getTopicName();
            }
        }

        throw new IllegalArgumentException("Unknown AgentStatTopic number. applicationName :" + applicationName + ". hashVaule :" + hashValue);
    }

    public static String getAgentStatTableName(String applicationName, int agentStatTopicCount) {
        int hashValue = getHashValue(applicationName, agentStatTopicCount);

        for (AgentStatTopicAndTable agentStatTopic : AgentStatTopicAndTable.values()) {
            if (agentStatTopic.getNumber() == hashValue) {
                return agentStatTopic.getTableName();
            }
        }

        throw new IllegalArgumentException("Unknown AgentStatTable number. applicationName :" + applicationName + ". hashVaule :" + hashValue);
    }

    private static int getHashValue(String applicationName, int agentStatTopicCount) {
        return Utils.toPositive(Utils.murmur2(applicationName.getBytes())) % agentStatTopicCount;
    }

    private enum AgentStatTopicAndTable {
        AGENT_STAT_00("inspector-stat-agent-00", "inspectorStatAgent00", 0),
        AGENT_STAT_01("inspector-stat-agent-01", "inspectorStatAgent01", 1),
        AGENT_STAT_02("inspector-stat-agent-02", "inspectorStatAgent02", 2),
        AGENT_STAT_03("inspector-stat-agent-03", "inspectorStatAgent03", 3),
        AGENT_STAT_04("inspector-stat-agent-04", "inspectorStatAgent04", 4),
        AGENT_STAT_05("inspector-stat-agent-05", "inspectorStatAgent05", 5),
        AGENT_STAT_06("inspector-stat-agent-06", "inspectorStatAgent06", 6),
        AGENT_STAT_07("inspector-stat-agent-07", "inspectorStatAgent07", 7),
        AGENT_STAT_08("inspector-stat-agent-08", "inspectorStatAgent08", 8),
        AGENT_STAT_09("inspector-stat-agent-09", "inspectorStatAgent09", 9),
        AGENT_STAT_10("inspector-stat-agent-10", "inspectorStatAgent10", 10),
        AGENT_STAT_11("inspector-stat-agent-11", "inspectorStatAgent11", 11),
        AGENT_STAT_12("inspector-stat-agent-12", "inspectorStatAgent12", 12),
        AGENT_STAT_13("inspector-stat-agent-13", "inspectorStatAgent13", 13),
        AGENT_STAT_14("inspector-stat-agent-14", "inspectorStatAgent14", 14),
        AGENT_STAT_15("inspector-stat-agent-15", "inspectorStatAgent15", 15);

        private final String topicName;

        private final String tableName;

        private final int number;
        AgentStatTopicAndTable(String topicName, String tableName, int number) {
            this.topicName = topicName;
            this.tableName = tableName;
            this.number = number;
        };

        public String getTopicName() {
            return topicName;
        }

        public String getTableName() {
            return tableName;
        }

        public int getNumber() {
            return number;
        }
    }

}
