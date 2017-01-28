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
import java.lang.reflect.Modifier;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author Sangyoon Lee
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({ "net.sf.json-lib:json-lib:jar:jdk15:(,)" })
public class JsonLibJSONSerializerIT {

    private static final String SERVICE_TYPE = "JSON-LIB";
    private static final String ANNOTATION_KEY = "json-lib.json.length";

    @Test
    public void test() throws Exception {
        Method toJSON = JSONSerializer.class.getMethod("toJSON", Object.class);
        Method toJava = JSONSerializer.class.getMethod("toJava", JSON.class);

        String test = "{'string':'JSON'}";

        JSON json = JSONSerializer.toJSON(test);

        if (Modifier.isStatic(toJava.getModifiers())) {
            toJava.invoke(null, json);
        } else {
            // JSONSerializer.toJava(JSON) of json-lib 2.0 and below is instance method.
            toJava.invoke(new JSONSerializer(), json);
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event(SERVICE_TYPE, toJSON, annotation(ANNOTATION_KEY, test.length())));
        verifier.verifyTrace(event("JSON-LIB", toJava));
        
        verifier.verifyTraceCount(0);
    }
}
