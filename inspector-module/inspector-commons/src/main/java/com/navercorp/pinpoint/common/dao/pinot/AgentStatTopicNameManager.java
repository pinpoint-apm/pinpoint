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
public class AgentStatTopicNameManager extends AgentStatNameManager {

    private final String topicPrefix;
    private final String numberFormat;

    public AgentStatTopicNameManager(String topicPrefix, int paddingLength) {
        this.topicPrefix = topicPrefix;
        this.numberFormat = "%0" + String.valueOf(paddingLength) + "d";
    }

    public String getAgentStatTopicName(String applicationName, int agentStatTopicCount) {
        int hashValue = getHashValue(applicationName, agentStatTopicCount);
        String postfix = String.format(numberFormat, hashValue);
        StringBuilder sb = new StringBuilder();
        sb.append(topicPrefix).append(postfix);
        return sb.toString();
    }
}
