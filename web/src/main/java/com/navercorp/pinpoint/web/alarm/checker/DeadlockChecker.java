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

package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.AgentEventDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DeadlockChecker extends AgentChecker<Boolean> {

    public DeadlockChecker(AgentEventDataCollector agentEventDataCollector, Rule rule) {
        super(rule, "BOOLEAN", agentEventDataCollector);
    }

    @Override
    protected boolean decideResult(Boolean value) {
        // 0 is set disable
        if (rule.getThreshold() > 0) {
            return value;
        }
        return false;
    }

    @Override
    protected Map<String, Boolean> getAgentValues() {
        return ((AgentEventDataCollector) dataCollector).getAgentDeadlockEventDetected();
    }

    public List<String> getSmsMessage() {
        List<String> messages = new LinkedList<>();

        for (Map.Entry<String, Boolean> detected : detectedAgents.entrySet()) {
            messages.add(String.format("[PINPOINT Alarm - %s] Deadlock thread detected", detected.getKey()));
        }

        return messages;
    }

    @Override
    public String getEmailMessage() {
        StringBuilder message = new StringBuilder();
        for (Map.Entry<String, Boolean> detected : detectedAgents.entrySet()) {
            message.append(String.format(" Value of agent(%s) has deadlocked thread during the past 5 mins.", detected.getKey()));
            message.append("<br>");
        }
        return message.toString();
    }

}
