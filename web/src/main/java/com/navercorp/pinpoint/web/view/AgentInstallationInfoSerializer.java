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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.vo.AgentInstallationInfo;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class AgentInstallationInfoSerializer  extends JsonSerializer<AgentInstallationInfo> {

    @Override
    public void serialize(AgentInstallationInfo installationInfo, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeStringField("version", installationInfo.getVersion());
        jgen.writeStringField("downloadUrl", installationInfo.getDownloadUrl());
        jgen.writeStringField("installationArgument", installationInfo.getJavaInstallationInfo());

        jgen.writeEndObject();
    }

}

