/*
 * Copyright 2014 NAVER Corp.
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

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @see JacksonPlugin#intercept_ObjectMapper(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
 * @author Sungkook Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"org.codehaus.jackson:jackson-mapper-asl:[1.0.1],[1.1.2],[1.2.1],[1.3.5],[1.4.5],[1.5.8],[1.6.9],[1.7.9],[1.8.11],[1.9.13]"})
public class ObjectMapper_1_x_IT {

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


        
        Constructor<?> omConstructor1 = ObjectMapper.class.getConstructor();
        Constructor<?> omConstructor2 = ObjectMapper.class.getConstructor(JsonFactory.class);
        Constructor<?> omConstructor3 = ObjectMapper.class.getConstructor(JsonFactory.class, SerializerProvider.class, DeserializerProvider.class);
        
        
        Class<?> serializationConfig = null;
        Class<?> deserializationConfig = null;
        
        try {
            serializationConfig = Class.forName("org.codehaus.jackson.map.SerializationConfig"); 
            deserializationConfig = Class.forName("org.codehaus.jackson.map.DeserializationConfig");
        } catch (ClassNotFoundException ignored) {
            
        }
        
        Constructor<?> omConstructor4 = null;
        
        if (serializationConfig != null && deserializationConfig != null) {
            omConstructor4 = ObjectMapper.class.getConstructor(JsonFactory.class, SerializerProvider.class, DeserializerProvider.class, serializationConfig, deserializationConfig);
        }
               

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        
        if (omConstructor4 != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, omConstructor4));
        }
        
        verifier.verifyTrace(event(SERVICE_TYPE, omConstructor3),
                event(SERVICE_TYPE, omConstructor1));
        
        if (omConstructor4 != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, omConstructor4));
        }
        
        verifier.verifyTrace(event(SERVICE_TYPE, omConstructor3),
                event(SERVICE_TYPE, omConstructor2));

        verifier.verifyTraceCount(0);
    }
    
    private Method getMethod(Class<?> targetClass, String name, Class<?>... paramTypes) {
        try {
            return targetClass.getMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Test()
    public void testWriteValue() throws Exception {
        __POJO pojo = new __POJO();
        pojo.setName("Jackson");

        Method mapperWriteValueAsString = getMethod(ObjectMapper.class, "writeValueAsString", Object.class);
        Method mapperWriteValueAsBytes = getMethod(ObjectMapper.class, "writeValueAsBytes", Object.class);
        Method mapperWriteValue = getMethod(ObjectMapper.class, "writeValue", Writer.class, Object.class);
        
        
        mapper.writeValue(new OutputStreamWriter(new ByteArrayOutputStream()), pojo);
        String jsonString = mapperWriteValueAsString == null ? null : (String)mapperWriteValueAsString.invoke(mapper, pojo);
        byte[] jsonBytes = mapperWriteValueAsBytes == null ? null : (byte[])mapperWriteValueAsBytes.invoke(mapper, pojo);
        
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();


        verifier.verifyTrace(event(SERVICE_TYPE, mapperWriteValue));
        
        if (mapperWriteValueAsString != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, mapperWriteValueAsString, annotation(ANNOTATION_KEY, jsonString.length())));
        }
        
        if (mapperWriteValueAsBytes != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, mapperWriteValueAsBytes, annotation(ANNOTATION_KEY, jsonBytes.length)));
        }

        verifier.verifyTraceCount(0);
    }

    @Test
    public void testReadValue() throws Exception {
        String jsonString = "{\"name\" : \"Jackson\"}";
        byte[] jsonBytes = jsonString.getBytes(UTF_8);
        
        Method mapperReadValueString = getMethod(ObjectMapper.class, "readValue", String.class, Class.class);
        Method mapperReadValueBytes = getMethod(ObjectMapper.class, "readValue", byte[].class, Class.class);
        
        Method mapperReader = getMethod(ObjectMapper.class, "reader", Class.class);
        
        Class<?> readerClass = null; 
        Method readerReadValueString = null;
        Method readerReadValueBytes = null;
        
        
        try {
            readerClass = Class.forName("org.codehaus.jackson.map.ObjectReader");
            readerReadValueString = getMethod(readerClass, "readValue", String.class);
            readerReadValueBytes = getMethod(readerClass, "readValue", byte[].class);
        } catch (ClassNotFoundException ignored) {
            
        }
        
        
        
        
        
        Object foo = mapper.readValue(jsonString, __POJO.class);
        foo = mapperReadValueBytes == null ? null : mapperReadValueBytes.invoke(mapper, jsonBytes, __POJO.class);
        
        if (mapperReader != null) {
            Object reader = mapperReader.invoke(mapper, __POJO.class);
            
            foo = readerReadValueString == null ? null : readerReadValueString.invoke(reader, jsonString);
            foo = readerReadValueBytes == null ? null : readerReadValueBytes.invoke(reader, jsonBytes);
        }

        
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();


        verifier.verifyTrace(event(SERVICE_TYPE, mapperReadValueString, Expectations.annotation(ANNOTATION_KEY, jsonString.length())));
        
        if (mapperReadValueBytes != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, mapperReadValueBytes, Expectations.annotation(ANNOTATION_KEY, jsonBytes.length)));
        }

        if (readerReadValueString != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, readerReadValueString, Expectations.annotation(ANNOTATION_KEY, jsonString.length())));
        }
        
        if (readerReadValueBytes != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, readerReadValueBytes, Expectations.annotation(ANNOTATION_KEY, jsonBytes.length)));
        }

        
        verifier.verifyTraceCount(0);
    }

    public static class __POJO {
        public String name;
        
        public String getName() { return name; }
        public void setName(String str) { name = str; }
    }
}

