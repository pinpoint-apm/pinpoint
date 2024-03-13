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
    public void serialize(WebhookPayload webhookPayload, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        
        gen.writeStringField("pinpointUrl", webhookPayload.getPinpointUrl());
        gen.writeStringField("batchEnv", webhookPayload.getBatchEnv());
        gen.writeStringField("applicationId", webhookPayload.getApplicationName());
        gen.writeStringField("serviceType", webhookPayload.getServiceType());
        gen.writeObjectField("userGroup", webhookPayload.getUserGroup());
        
        writeChecker(webhookPayload, gen);
        
        gen.writeStringField("unit", webhookPayload.getUnit());
        Number threshold = webhookPayload.getThreshold();
        if (threshold instanceof Integer integer) {
            gen.writeNumberField("threshold", integer);
        } else if (threshold instanceof BigDecimal bigDecimal){
            gen.writeNumberField("threshold", bigDecimal);
        } else {
            throw new IOException("threshold type should be either Integer or BigDecimal");
        }
        gen.writeStringField("notes", webhookPayload.getNotes());
        gen.writeNumberField("sequenceCount", webhookPayload.getSequenceCount());
        
        gen.writeEndObject();
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
