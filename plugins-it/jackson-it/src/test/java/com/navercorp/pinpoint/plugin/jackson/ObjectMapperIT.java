/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jackson;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @see JacksonPlugin#intercept_ObjectMapper(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
 * @author Sungkook Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-jackson-plugin")
// 2.7.0, 2.7.1 has JDK6 compatibility issue - https://github.com/FasterXML/jackson-databind/issues/1134
@Dependency({"com.fasterxml.jackson.core:jackson-databind:[2.0.6],[2.1.5],[2.2.4],[2.3.4],[2.4.6],[2.5.4,2.6.max],[2.7.2,2.7.max]"})
public class ObjectMapperIT {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * 
     */
    private static final String ANNOTATION_KEY = "jackson.json.length";
    /**
     * 
     */
    private static final String SERVICE_TYPE = "JACKSON";
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testConstructor() throws Exception {
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
    public void testWriteValue() throws Exception {
        __POJO pojo = new __POJO();
        pojo.setName("Jackson");

        String jsonStr = mapper.writeValueAsString(pojo);
        byte[] jsonByte = mapper.writeValueAsBytes(pojo);
        
        ObjectWriter writer = mapper.writer();

        writer.writeValueAsString(pojo);
        writer.writeValueAsBytes(pojo);


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
    public void testReadValue() throws Exception {
        String json_str = "{\"name\" : \"Jackson\"}";
        byte[] json_b = json_str.getBytes(UTF_8);
    
        mapper.readValue(json_str, __POJO.class);
        mapper.readValue(json_b, __POJO.class);
        
        ObjectReader reader = mapper.reader(__POJO.class);
        
        reader.readValue(json_str);
        reader.readValue(json_b);

        
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
    
    private static class __POJO {
        public String name;

        public String getName() { return name; }
        public void setName(String str) { name = str; }
    }
}


