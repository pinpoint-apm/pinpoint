/**
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

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;

import java.lang.reflect.Method;

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
@Dependency({"net.sf.json-lib:json-lib:jar:jdk15:[1.0,)"})
public class JsonLibJSONObjectIT {

    private static final String test = "{'string':'JSON'}";
    private static final String json = JSONObject.fromObject(test).toString();
    private static final String serviceType = "JSON-LIB";
    private static final String annotationKeyName = "json-lib.json.length";
    // "Pinpoint"
    private static final PluginTestVerifier.ExpectedAnnotation fromObjectAnnotation = annotation(annotationKeyName, json.length());
    // "Pinpoint"
    private static final PluginTestVerifier.ExpectedAnnotation toStringAnnotation = annotation(annotationKeyName, json.length());

    @Test
    public void jsonToBeanTest() throws Exception {
        String test = "{'string':'JSON'}";

        JSONObject jsn = JSONObject.fromObject(test);
        JSONObject.toBean(jsn);	
        jsn.toString();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);
        
        Method fromObject = JSONObject.class.getMethod("fromObject", Object.class);
        Method toBean = JSONObject.class.getMethod("toBean", JSONObject.class);
        Method toString  = JSONObject.class.getMethod("toString");

        verifier.verifyTraceBlock(PluginTestVerifier.BlockType.EVENT, serviceType, fromObject, null, null, null, null, fromObjectAnnotation);
        verifier.verifyApi("JSON-LIB", toBean);
        verifier.verifyTraceBlock(PluginTestVerifier.BlockType.EVENT, serviceType, toString, null, null, null, null, toStringAnnotation);

        verifier.verifyTraceBlockCount(0);
    }
}
