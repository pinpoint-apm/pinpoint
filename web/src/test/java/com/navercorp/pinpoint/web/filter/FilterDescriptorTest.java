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

package com.navercorp.pinpoint.web.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author netspider
 */
public class FilterDescriptorTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void convert() throws IOException {

        String jsonString = writeJsonString();

        FilterDescriptor descriptor = mapper.readValue(jsonString, FilterDescriptor.class);

        FilterDescriptor.ResponseTime responseTime = descriptor.getResponseTime();
        FilterDescriptor.FromNode fromNode = descriptor.getFromNode();
        Assertions.assertEquals("FROM_APPLICATION", fromNode.getApplicationName());
        Assertions.assertEquals("FROM_SERVICE_TYPE", fromNode.getServiceType());
        Assertions.assertEquals("FROM_AGENT_ID", fromNode.getAgentId());
        Assertions.assertEquals((Long) 0L, descriptor.getResponseTime().getFromResponseTime());

        FilterDescriptor.ToNode toNode = descriptor.getToNode();
        Assertions.assertEquals("TO_APPLICATION", toNode.getApplicationName());
        Assertions.assertEquals("TO_SERVICE_TYPE", toNode.getServiceType());
        Assertions.assertEquals("TO_AGENT_ID", toNode.getAgentId());
        Assertions.assertEquals((Long) 1000L, responseTime.getToResponseTime());

        Assertions.assertEquals(Boolean.TRUE, descriptor.getOption().getIncludeException());
        Assertions.assertEquals("/**", descriptor.getOption().getUrlPattern());
    }

    private String writeJsonString() throws IOException {
        StringWriter writer = new StringWriter();

        JsonGenerator json = mapper.getFactory().createGenerator(writer);

//        json.writeStartArray();
        json.writeStartObject();
        json.writeStringField("fa", "FROM_APPLICATION");
        json.writeStringField("fst", "FROM_SERVICE_TYPE");
        json.writeStringField("fan", "FROM_AGENT_ID");
        // fromResponseTime
        json.writeNumberField("rf", 0);

        json.writeStringField("ta", "TO_APPLICATION");
        json.writeStringField("tst", "TO_SERVICE_TYPE");
        json.writeStringField("tan", "TO_AGENT_ID");
        // toResponseTime
        json.writeNumberField("rt", 1000);

        json.writeNumberField("ie", 1);

        json.writeStringField("url", Base64.encodeBytes("/**".getBytes(StandardCharsets.UTF_8)));
        json.writeEndObject();
//        json.writeEndArray();

        json.flush();
        json.close();

        String jsonString = writer.toString();
        logger.debug("json:{}", jsonString);
        return jsonString;
    }

    @Test
    public void convert_array() throws IOException {

        String arrayJson = "[" + writeJsonString() + "]";


        logger.debug("json:{}", arrayJson);

        List<FilterDescriptor> descriptor = mapper.readValue(arrayJson, new TypeReference<List<FilterDescriptor>>() {
        });

        Assertions.assertEquals(1, descriptor.size());
        Assertions.assertNotNull(descriptor.get(0));
    }

    @Test
    public void invalidJson() throws IOException {
        Assertions.assertThrows(IOException.class, () -> {
            mapper.readValue("INVALID", new TypeReference<List<FilterDescriptor>>() {
            });
        });
    }
}
