/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.vo.sender.payload;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

import java.math.BigDecimal;

/**
 * @author Jongjin.Bae
 */
public class WebhookPayloadSerializer extends ValueSerializer<WebhookPayload> {
    
    @Override
    public void serialize(WebhookPayload webhookPayload, JsonGenerator jgen, SerializationContext serializers) {
        jgen.writeStartObject();
        
        jgen.writeStringProperty("pinpointUrl", webhookPayload.getPinpointUrl());
        jgen.writeStringProperty("batchEnv", webhookPayload.getBatchEnv());
        jgen.writeStringProperty("applicationId", webhookPayload.getApplicationName());
        jgen.writeStringProperty("serviceType", webhookPayload.getServiceType());

        writeChecker(webhookPayload, jgen);
        
        jgen.writeStringProperty("unit", webhookPayload.getUnit());
        Number threshold = webhookPayload.getThreshold();
        if (threshold instanceof Integer integer) {
            jgen.writeNumberProperty("threshold", integer);
        } else if (threshold instanceof BigDecimal bigDecimal){
            jgen.writeNumberProperty("threshold", bigDecimal);
        } else {
            throw new IllegalStateException("threshold type should be either Integer or BigDecimal");
        }
        jgen.writeStringProperty("notes", webhookPayload.getNotes());
        jgen.writeNumberProperty("sequenceCount", webhookPayload.getSequenceCount());
        
        jgen.writeEndObject();
    }
    
    private void writeChecker(WebhookPayload webhookPayload, JsonGenerator jgen) {
        jgen.writeName("checker");
        jgen.writeStartObject();
        
        jgen.writeStringProperty("name", webhookPayload.getCheckerName());
        String checkerType = webhookPayload.getCheckerType();
        jgen.writeStringProperty("type", checkerType);
        CheckerDetectedValue checkerDetectedValue = webhookPayload.getCheckerDetectedValue();
        
        if (checkerType.equals("LongValueAlarmChecker")) {
            jgen.writePOJOProperty("detectedValue", ((AlarmCheckerDetectedValue) checkerDetectedValue).getDetectedValue());
        }
        
        if (checkerType.equals("LongValueAgentChecker")) {
            jgen.writePOJOProperty("detectedValue", ((AgentCheckerDetectedValue) checkerDetectedValue).getDetectedAgents());
        }
        
        if (checkerType.equals("BooleanValueAgentChecker")) {
            jgen.writePOJOProperty("detectedValue", ((AgentCheckerDetectedValue) checkerDetectedValue).getDetectedAgents());
        }
        
        if (checkerType.equals("DataSourceAlarmListValueAgentChecker")) {
            jgen.writePOJOProperty("detectedValue", ((AgentCheckerDetectedValue) checkerDetectedValue).getDetectedAgents());
        }
        
        jgen.writeEndObject();
    }
}
