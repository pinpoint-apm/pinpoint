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

package com.navercorp.pinpoint.inspector.collector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author minwoo-jung
 */
@Component
public class InspectorCollectorProperties {

    @Value("${kafka.inspector.agent.topic.name}")
    private String agentStatTopicName;
    @Value("${kafka.inspector.agent.topic.count}")
    private int agentStatTopicCount;
    @Value("${kafka.inspector.application.topic.name}")
    private String applicationStatTopicName;

    public String getAgentStatTopicName() {
        return agentStatTopicName;
    }

    public int getAgentStatTopicCount() {
        return agentStatTopicCount;
    }

    public String getApplicationStatTopicName() {
        return applicationStatTopicName;
    }
}
