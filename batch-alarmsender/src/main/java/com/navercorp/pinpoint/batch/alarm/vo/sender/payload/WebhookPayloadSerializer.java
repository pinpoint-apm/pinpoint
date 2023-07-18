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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Jongjin.Bae
 */
public class WebhookPayloadSerializer extends JsonSerializer<WebhookPayload> {
    
    @Override
    public void serialize(WebhookPayload webhookPayload, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
        jgen.writeStartObject();
        
        jgen.writeStringField("pinpointUrl", webhookPayload.getPinpointUrl());
        jgen.writeStringField("batchEnv", webhookPayload.getBatchEnv());
        jgen.writeStringField("applicationId", webhookPayload.getApplicationId());
        jgen.writeStringField("serviceType", webhookPayload.getServiceType());
        jgen.writeObjectField("userGroup", webhookPayload.getUserGroup());
        
        writeChecker(webhookPayload, jgen);
        
        jgen.writeStringField("unit", webhookPayload.getUnit());
        Number threshold = webhookPayload.getThreshold();
        if (threshold instanceof Integer) {
            jgen.writeNumberField("threshold", (Integer) threshold);
        } else if (threshold instanceof BigDecimal){
            jgen.writeNumberField("threshold", (BigDecimal) threshold);
        } else {
            throw new IOException("threshold type should be either Integer or BigDecimal");
        }
        jgen.writeStringField("notes", webhookPayload.getNotes());
        jgen.writeNumberField("sequenceCount", webhookPayload.getSequenceCount());
        
        jgen.writeEndObject();
    }
    
    private void writeChecker(WebhookPayload webhookPayload, JsonGenerator jgen) throws IOException {
        jgen.writeFieldName("checker");
        jgen.writeStartObject();
        
        jgen.writeStringField("name", webhookPayload.getCheckerName());
        String checkerType = webhookPayload.getCheckerType();
        jgen.writeStringField("type", checkerType);
        CheckerDetectedValue checkerDetectedValue = webhookPayload.getCheckerDetectedValue();
        
        if (checkerType.equals("LongValueAlarmChecker")) {
            jgen.writeObjectField("detectedValue", ((AlarmCheckerDetectedValue) checkerDetectedValue).getDetectedValue());
        }
        
        if (checkerType.equals("LongValueAgentChecker")) {
            jgen.writeObjectField("detectedValue", ((AgentCheckerDetectedValue) checkerDetectedValue).getDetectedAgents());
        }
        
        if (checkerType.equals("BooleanValueAgentChecker")) {
            jgen.writeObjectField("detectedValue", ((AgentCheckerDetectedValue) checkerDetectedValue).getDetectedAgents());
        }
        
        if (checkerType.equals("DataSourceAlarmListValueAgentChecker")) {
            jgen.writeObjectField("detectedValue", ((AgentCheckerDetectedValue) checkerDetectedValue).getDetectedAgents());
        }
        
        jgen.writeEndObject();
    }
}
