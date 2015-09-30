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
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 *
 * @author netspider
 *
 */
public class FilterDescriptorTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void convert() throws IOException {

        String jsonString = writeJsonString();

        FilterDescriptor descriptor = mapper.readValue(jsonString, FilterDescriptor.class);

        Assert.assertEquals("FROM_APPLICATION", descriptor.getFromApplicationName());
        Assert.assertEquals("FROM_SERVICE_TYPE", descriptor.getFromServiceType());
        Assert.assertEquals("FROM_AGENT_ID", descriptor.getFromAgentName());
        Assert.assertEquals((Long)0L, descriptor.getFromResponseTime());

        Assert.assertEquals("TO_APPLICATION", descriptor.getToApplicationName());
        Assert.assertEquals("TO_SERVICE_TYPE", descriptor.getToServiceType());
        Assert.assertEquals("TO_AGENT_ID", descriptor.getToAgentName());
        Assert.assertEquals((Long)1000L, descriptor.getResponseTo());

        Assert.assertEquals(Boolean.TRUE, descriptor.getIncludeException());
        Assert.assertEquals("/**", descriptor.getUrlPattern());
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

        json.writeStringField("url", "/**");
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

        String arrayJson = "["+writeJsonString() + "]";


        logger.debug("json:{}", arrayJson);

        List<FilterDescriptor> descriptor = mapper.readValue(arrayJson, new TypeReference<List<FilterDescriptor>>() {
        });

        Assert.assertEquals(1, descriptor.size());
        Assert.assertNotNull(descriptor.get(0));
    }

    @Test(expected = IOException.class)
    public void invalidJson() throws IOException {

        mapper.readValue("INVALID", new TypeReference<List<FilterDescriptor>>() {
        });

    }
}
