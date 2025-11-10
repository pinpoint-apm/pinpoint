/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.navercorp.pinpoint.common.server.util.json.JacksonWriterUtils;

import java.io.IOException;
import java.util.Objects;

public class AgentServerGroupListWriter {

    public void write(String fieldName, ServerGroupList serverGroups, JsonGenerator jgen) throws IOException {
        Objects.requireNonNull(fieldName, "fieldName");

        if (serverGroups == null) {
            JacksonWriterUtils.writeEmptyArray(jgen, fieldName);
            return;
        }

        jgen.writeFieldName(fieldName);
        jgen.writeStartArray();
        for (ServerGroup serverGroup : serverGroups.getServerGroupList()) {
            for (ServerInstance serverInstance : serverGroup.getInstanceList()) {
                jgen.writeStartObject();
                jgen.writeStringField("id", serverInstance.getName());
                jgen.writeStringField("name", serverInstance.getAgentName());
                jgen.writeEndObject();
            }
        }
        jgen.writeEndArray();
    }
}
