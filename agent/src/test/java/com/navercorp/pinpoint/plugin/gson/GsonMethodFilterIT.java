/**
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
package com.navercorp.pinpoint.plugin.gson;

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ChaYoung You
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.google.code.gson:gson:2.3.1"})
public class GsonMethodFilterIT {
    @Test
    public void test() throws Exception {
        final String json = "Pinpoint";
        final String serviceType = "GSON";
        final String annotationKeyName = "JSON_LENGTH";
        // Pinpoint
        final PluginTestVerifier.ExpectedAnnotation fromJsonAnnotation = annotation(annotationKeyName, json.length());
        // "Pinpoint"
        final PluginTestVerifier.ExpectedAnnotation toJsonAnnotation = annotation(annotationKeyName, json.length() + 2);
        final Gson gson = new Gson();
        final JsonElement jsonElement = new JsonParser().parse(json);

        /**
         * @see Gson#fromJson(String, Class)
         * @see Gson#fromJson(String, Type)
         * @see Gson#fromJson(Reader, Class)
         * @see Gson#fromJson(Reader, Type)
         * @see Gson#fromJson(JsonReader, Type)
         * @see Gson#fromJson(JsonElement, Class)
         * @see Gson#fromJson(JsonElement, Type)
         */
        gson.fromJson(json, String.class);
        gson.fromJson(json, (Type) String.class);
        gson.fromJson(new StringReader(json), (Class) String.class);
        gson.fromJson(new StringReader(json), (Type) String.class);
        gson.fromJson(new JsonReader(new StringReader(json)), String.class);
        gson.fromJson(jsonElement, String.class);
        gson.fromJson(jsonElement, (Type) String.class);

        Method fromJson1 = Gson.class.getDeclaredMethod("fromJson", String.class, Class.class);
        Method fromJson2 = Gson.class.getDeclaredMethod("fromJson", String.class, Type.class);
        Method fromJson3 = Gson.class.getDeclaredMethod("fromJson", Reader.class, Class.class);
        Method fromJson4 = Gson.class.getDeclaredMethod("fromJson", Reader.class, Type.class);
        Method fromJson5 = Gson.class.getDeclaredMethod("fromJson", JsonReader.class, Type.class);
        Method fromJson6 = Gson.class.getDeclaredMethod("fromJson", JsonElement.class, Class.class);
        Method fromJson7 = Gson.class.getDeclaredMethod("fromJson", JsonElement.class, Type.class);

        /**
         * @see Gson#toJson(Object)
         * @see Gson#toJson(Object, Type)
         * @see Gson#toJson(Object, Appendable)
         * @see Gson#toJson(Object, Type, Appendable)
         * @see Gson#toJson(Object, Type, JsonWriter)
         * @see Gson#toJson(JsonElement)
         * @see Gson#toJson(JsonElement, Appendable)
         * @see Gson#toJson(JsonElement, JsonWriter)
         */
        gson.toJson(json);
        gson.toJson(json, String.class);
        gson.toJson(json, new StringWriter());
        gson.toJson(json, String.class, new StringWriter());
        gson.toJson(json, String.class, new JsonWriter(new StringWriter()));
        gson.toJson(jsonElement);
        gson.toJson(jsonElement, new StringWriter());
        gson.toJson(jsonElement, new JsonWriter(new StringWriter()));

        Method toJson1 = Gson.class.getDeclaredMethod("toJson", Object.class);
        Method toJson2 = Gson.class.getDeclaredMethod("toJson", Object.class, Type.class);
        Method toJson3 = Gson.class.getDeclaredMethod("toJson", Object.class, Appendable.class);
        Method toJson4 = Gson.class.getDeclaredMethod("toJson", Object.class, Type.class, Appendable.class);
        Method toJson5 = Gson.class.getDeclaredMethod("toJson", Object.class, Type.class, JsonWriter.class);
        Method toJson6 = Gson.class.getDeclaredMethod("toJson", JsonElement.class);
        Method toJson7 = Gson.class.getDeclaredMethod("toJson", JsonElement.class, Appendable.class);
        Method toJson8 = Gson.class.getDeclaredMethod("toJson", JsonElement.class, JsonWriter.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);

        verifyTraceBlockAnnotation(verifier, serviceType, fromJson1, fromJsonAnnotation);
        verifyTraceBlockAnnotation(verifier, serviceType, fromJson2, fromJsonAnnotation);
        verifier.verifyApi(serviceType, fromJson3);
        verifier.verifyApi(serviceType, fromJson4);
        verifier.verifyApi(serviceType, fromJson5);
        verifier.verifyApi(serviceType, fromJson6);
        verifier.verifyApi(serviceType, fromJson7);

        verifyTraceBlockAnnotation(verifier, serviceType, toJson1, toJsonAnnotation);
        verifyTraceBlockAnnotation(verifier, serviceType, toJson2, toJsonAnnotation);
        verifier.verifyApi(serviceType, toJson3);
        verifier.verifyApi(serviceType, toJson4);
        verifier.verifyApi(serviceType, toJson5);
        verifyTraceBlockAnnotation(verifier, serviceType, toJson6, toJsonAnnotation);
        verifier.verifyApi(serviceType, toJson7);
        verifier.verifyApi(serviceType, toJson8);

        // No more traces
        verifier.verifyTraceBlockCount(0);
    }

    private void verifyTraceBlockAnnotation(PluginTestVerifier verifier, String serviceType, Member api, PluginTestVerifier.ExpectedAnnotation annotation) {
        verifier.verifyTraceBlock(PluginTestVerifier.BlockType.EVENT, serviceType, api, null, null, null, null, annotation);
    }
}
