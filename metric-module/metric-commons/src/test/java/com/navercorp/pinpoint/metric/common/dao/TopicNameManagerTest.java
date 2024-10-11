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

package com.navercorp.pinpoint.metric.common.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo-jung
 */
class TopicNameManagerTest {

    @Test
    public void getTopicNameTest() {
        String topicPrefix = "inspector-stat-agent-";
        TopicNameManager topicNameManager = new TopicNameManager(topicPrefix, 2, 16);
        String agentStatTopicName = topicNameManager.getTopicName("testApplication");
        assertEquals(topicPrefix + "06", agentStatTopicName);
    }

    @Test
    public void getTopicNameTest2() {
        String topicPrefix = "inspector-stat-agent-";
        TopicNameManager topicNameManager = new TopicNameManager(topicPrefix, 3, 16);
        String agentStatTopicName = topicNameManager.getTopicName("testApplication");
        assertEquals(topicPrefix + "006", agentStatTopicName);
    }


}