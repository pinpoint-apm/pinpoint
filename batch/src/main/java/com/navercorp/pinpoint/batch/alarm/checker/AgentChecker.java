/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.AgentCheckerDetectedValue;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.CheckerDetectedValue;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.DetectedAgent;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author minwoo.jung
 * @author Jongjin.Bae
 */
public abstract class AgentChecker<T> extends AlarmChecker<T> {
    
    protected final Map<String, T> detectedAgents = new HashMap<>();

    protected AgentChecker(Rule rule, String unit, DataCollector dataCollector) {
        super(rule, unit, dataCollector);
    }
    
    @Override
    public void check() {
        dataCollector.collect();

        Map<String, T> agents = getAgentValues();
        
        for(Entry<String, T> agent : agents.entrySet()) {
            if (decideResult(agent.getValue())) {
                detected = true;
                detectedAgents.put(agent.getKey(), agent.getValue());
            }
            
            logger.info("{} result is {} for agent({}). value is {}. (threshold : {}).", this.getClass().getSimpleName(), detected, agent.getKey(), agent.getValue(), rule.getThreshold());
        }
    }
    
    @Override
    protected T getDetectedValue() {
        throw new UnsupportedOperationException(this.getClass() + "is not support getDetectedValue function. you should use getAgentValues");
    }

    public List<String> getSmsMessage() {
        List<String> messages = new ArrayList<>();
        
        for (Entry<String, T> detected : detectedAgents.entrySet()) {
            messages.add(String.format("[PINPOINT Alarm - %s] %s is %s%s (Threshold : %s%s)", detected.getKey(), rule.getCheckerName(), detected.getValue(), unit, rule.getThreshold(), unit));
        }
        
        return messages;
    }

    protected static final String INSPECTOR_LINK_FORMAT = " <a href=\"%s/inspector/%s@%s/5m/%s/%s\" >inspector of %s</a>";
    protected static final String LINE_FEED = "<br>";

    @Override
    public String getEmailMessage(String pinpointUrl, String applicationId, String serviceType, String currentTime) {
        StringBuilder message = new StringBuilder();
        
        for (Entry<String, T> detected : detectedAgents.entrySet()) {
            String agentId = detected.getKey();
            message.append(String.format(" Value of agent(%s) is %s%s during the past 5 mins.(Threshold : %s%s)", agentId, detected.getValue(), unit, rule.getThreshold(), unit));
            message.append(String.format(INSPECTOR_LINK_FORMAT, pinpointUrl, applicationId, serviceType, currentTime, agentId, agentId));
            message.append(LINE_FEED);
        }
        
        return message.toString();
    }
    
    public Map<String, T> getDetectedAgents() { return detectedAgents; }
    
    protected abstract Map<String, T> getAgentValues();
    
    @Override
    public CheckerDetectedValue getCheckerDetectedValue() {
        List<DetectedAgent<T>> detectedAgents = new ArrayList<>();
        
        for (Map.Entry<String, T> entry : this.detectedAgents.entrySet()) {
            detectedAgents.add(new DetectedAgent<>(entry.getKey(), entry.getValue()));
        }
        
        return new AgentCheckerDetectedValue<>(detectedAgents);
    }
    
}
