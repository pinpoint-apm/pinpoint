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
package com.navercorp.pinpoint.plugin.json_lib;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.lang.reflect.Method;

import net.sf.json.JSONArray;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author Sangyoon Lee
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({ "net.sf.json-lib:json-lib:jar:jdk15:(,)" })
public class JsonLibJSONArrayIT {

    private static final String SERVICE_TYPE = "JSON-LIB";
    private static final String ANNOTATION_KEY = "json-lib.json.length";

    @SuppressWarnings("deprecation")
    @Test
    public void jsonToArrayTest() throws Exception {
        Method fromObject = JSONArray.class.getMethod("fromObject", Object.class);
        Method toArray = JSONArray.class.getMethod("toArray", JSONArray.class);
        Method toList = JSONArray.class.getMethod("toList", JSONArray.class);

        // JSONArray.toCollection() is added in json-lib 2.2. so check toCollection in JSONArray
        Method toCollection = null;
        try {
            toCollection = JSONArray.class.getMethod("toCollection", JSONArray.class);
        } catch (NoSuchMethodException ignored) {
        }

        String json = "[{'string':'JSON'}]";

        JSONArray jsonArray = JSONArray.fromObject(json);

        // JSONArray.toArray() of json-lib 2.0 and below have different return type. so we invoke it by reflection to avoid NoSuchMethodError
        toArray.invoke(null, jsonArray);

        JSONArray.toList(jsonArray);

        if (toCollection != null) {
            JSONArray.toCollection(jsonArray);
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event(SERVICE_TYPE, fromObject, annotation(ANNOTATION_KEY, json.length())));
        verifier.verifyTrace(event(SERVICE_TYPE, toArray));
        verifier.verifyTrace(event(SERVICE_TYPE, toList));
        
        if (toCollection != null) {
            verifier.verifyTrace(event(SERVICE_TYPE, toCollection));
        }

        verifier.verifyTraceCount(0);
    }

    @Test
    public void arrayToJsonTest() throws Exception {
        Method fromObject = JSONArray.class.getMethod("fromObject", Object.class);
        Method toString = JSONArray.class.getMethod("toString");

        JSONArray jsonArray = JSONArray.fromObject(new Object[] { "pinpoint", "json-lib" });
        String json = jsonArray.toString();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event(SERVICE_TYPE, fromObject));
        verifier.verifyTrace(event(SERVICE_TYPE, toString, Expectations.annotation(ANNOTATION_KEY, json.length())));

        verifier.verifyTraceCount(0);
    }
}
