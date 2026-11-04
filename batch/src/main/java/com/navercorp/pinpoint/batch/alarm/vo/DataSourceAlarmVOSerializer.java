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

package com.navercorp.pinpoint.batch.alarm.vo;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

/**
 * @author Jongjin.Bae
 */
public class DataSourceAlarmVOSerializer extends ValueSerializer<DataSourceAlarmVO> {
    
    @Override
    public void serialize(DataSourceAlarmVO dataSourceAlarmVO, JsonGenerator jgen, SerializationContext serializers) {
        jgen.writeStartObject();
        
        jgen.writeStringProperty("databaseName", dataSourceAlarmVO.getDatabaseName());

        jgen.writeNumberProperty("activeConnectionAvg", dataSourceAlarmVO.getActiveConnectionAvg());
        jgen.writeNumberProperty("maxConnectionAvg", dataSourceAlarmVO.getMaxConnectionAvg());

        jgen.writeNumberProperty("connectionValue", dataSourceAlarmVO.getConnectionUsedRate());
        
        jgen.writeEndObject();
    }
}
