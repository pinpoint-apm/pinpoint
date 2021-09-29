/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jackson;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Sungkook Kim
 */
public abstract class JacksonITBase {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final String ANNOTATION_KEY = "jackson.json.length";
    private static final String SERVICE_TYPE = "JACKSON";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void constructorTest() throws Exception {
        ObjectMapper mapper1 = new ObjectMapper();
        ObjectMapper mapper2 = new ObjectMapper(new JsonFactory());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Constructor<?> omConstructor = ObjectMapper.class.getConstructor(JsonFactory.class, DefaultSerializerProvider.class, DefaultDeserializationContext.class);
        Constructor<?> omConstructor1 = ObjectMapper.class.getConstructor();
        Constructor<?> omConstructor2 = ObjectMapper.class.getConstructor(JsonFactory.class);
        verifier.verifyTrace(event(SERVICE_TYPE, omConstructor));
        verifier.verifyTrace(event(SERVICE_TYPE, omConstructor1));
        verifier.verifyTrace(event(SERVICE_TYPE, omConstructor));
        verifier.verifyTrace(event(SERVICE_TYPE, omConstructor2));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void mapperWriteValueTest() throws Exception {
        __POJO pojo = new __POJO();
        pojo.setName("Jackson");

        String jsonStr = mapper.writeValueAsString(pojo);
        byte[] jsonByte = mapper.writeValueAsBytes(pojo);

        writeValueAsString(pojo);
        writeValueAsBytes(pojo);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method mapperWriteValueAsString = ObjectMapper.class.getMethod("writeValueAsString", Object.class);
        Method mapperWriteValueAsBytes = ObjectMapper.class.getMethod("writeValueAsBytes", Object.class);
        Method writerWriteValueAsString = ObjectWriter.class.getMethod("writeValueAsString", Object.class);
        Method writerWriteValueAsBytes = ObjectWriter.class.getMethod("writeValueAsBytes", Object.class);


        verifier.verifyTrace(event(SERVICE_TYPE, mapperWriteValueAsString, annotation(ANNOTATION_KEY, jsonStr.length())));
        verifier.verifyTrace(event(SERVICE_TYPE, mapperWriteValueAsBytes, annotation(ANNOTATION_KEY, jsonByte.length)));

        verifier.verifyTrace(event(SERVICE_TYPE, writerWriteValueAsString, annotation(ANNOTATION_KEY, jsonStr.length())));
        verifier.verifyTrace(event(SERVICE_TYPE, writerWriteValueAsBytes, annotation(ANNOTATION_KEY, jsonByte.length)));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void mapperReadValueTest() throws Exception {
        String json_str = "{\"name\" : \"Jackson\"}";
        byte[] json_b = json_str.getBytes(UTF_8);

        mapper.readValue(json_str, __POJO.class);
        mapper.readValue(json_b, __POJO.class);

        readValue(json_str);
        readValue(json_b);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method mapperReadValueString = ObjectMapper.class.getMethod("readValue", String.class, Class.class);
        Method mapperReadValueBytes = ObjectMapper.class.getMethod("readValue", byte[].class, Class.class);
        Method readerReadValueString = ObjectReader.class.getMethod("readValue", String.class);
        Method readerReadValueBytes = ObjectReader.class.getMethod("readValue", byte[].class);

        verifier.verifyTrace(event(SERVICE_TYPE, mapperReadValueString, annotation(ANNOTATION_KEY, json_str.length())));
        verifier.verifyTrace(event(SERVICE_TYPE, mapperReadValueBytes, annotation(ANNOTATION_KEY, json_b.length)));
        verifier.verifyTrace(event(SERVICE_TYPE, readerReadValueString, annotation(ANNOTATION_KEY, json_str.length())));
        verifier.verifyTrace(event(SERVICE_TYPE, readerReadValueBytes, annotation(ANNOTATION_KEY, json_b.length)));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void readerWriteValueTest() throws Exception {
        __POJO pojo = new __POJO();
        pojo.setName("Jackson");

        String jsonStr = writeValueAsString(pojo);
        byte[] jsonByte = writeValueAsBytes(pojo);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method writeval1 = ObjectWriter.class.getMethod("writeValueAsString", Object.class);
        Method writeval2 = ObjectWriter.class.getMethod("writeValueAsBytes", Object.class);

        verifier.verifyTrace(event("JACKSON", writeval1, annotation("jackson.json.length", jsonStr.length())));
        verifier.verifyTrace(event("JACKSON", writeval2, annotation("jackson.json.length", jsonByte.length)));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void readerReadValueTest() throws Exception {
        String json_str = "{\"name\" : \"Jackson\"}";
        byte[] json_b = json_str.getBytes(UTF_8);

        __POJO pojo = readValue(json_str);
        pojo = readValue(json_b);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method readval1 = ObjectReader.class.getMethod("readValue", String.class);
        Method readval2 = ObjectReader.class.getMethod("readValue", byte[].class);

        verifier.verifyTrace(event("JACKSON", readval1, Expectations.annotation("jackson.json.length", json_str.length())));
        verifier.verifyTrace(event("JACKSON", readval2, Expectations.annotation("jackson.json.length", json_b.length)));

        verifier.verifyTraceCount(0);
    }


    @SuppressWarnings("deprecation")
    private <T> T readValue(String src) throws JsonProcessingException {
        ObjectReader reader = mapper.reader(__POJO.class);
        return reader.readValue(src);
    }

    @SuppressWarnings("deprecation")
    private <T> T readValue(byte[] content) throws IOException {
        ObjectReader reader = mapper.reader(__POJO.class);
        return reader.readValue(content);
    }

    private String writeValueAsString(Object object) throws JsonProcessingException {
        ObjectWriter writer = mapper.writer();
        return writer.writeValueAsString(object);
    }

    private byte[] writeValueAsBytes(Object object) throws JsonProcessingException {
        ObjectWriter writer = mapper.writer();
        return writer.writeValueAsBytes(object);
    }

    private static class __POJO {
        public String name;

        public String getName() { return name; }
        public void setName(String str) { name = str; }
    }

}
