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
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 *@author Sangyoon Lee
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"net.sf.json-lib:json-lib:jar:jdk15:(,)"})
public class JsonLibJSONObjectIT {

    private static final String SERVICE_TYPE = "JSON-LIB";
    private static final String ANNOTATION_KEY = "json-lib.json.length";

    @Test
    public void jsonToBeanTest() throws Exception {
        String json = "{'string':'JSON'}";

        JSONObject jsonObject = JSONObject.fromObject(json);
        JSONObject.toBean(jsonObject);	

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        Method fromObject = JSONObject.class.getMethod("fromObject", Object.class);
        Method toBean = JSONObject.class.getMethod("toBean", JSONObject.class);

        verifier.verifyTrace(event(SERVICE_TYPE, fromObject, annotation(ANNOTATION_KEY, json.length())));
        verifier.verifyTrace(event(SERVICE_TYPE, toBean));

        verifier.verifyTraceCount(0);
    }
    
    
    @Test
    public void mapToJsonTest() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("name", "pinpoint");
        map.put("lib", "json-lib");

        JSONObject jsonObject = JSONObject.fromObject(map);
        String json = jsonObject.toString();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        Method fromObject = JSONObject.class.getMethod("fromObject", Object.class);
        Method toString  = JSONObject.class.getMethod("toString");

        verifier.verifyTrace(event(SERVICE_TYPE, fromObject));
        verifier.verifyTrace(event(SERVICE_TYPE, toString, annotation(ANNOTATION_KEY, json.length())));

        verifier.verifyTraceCount(0);
    }
}
