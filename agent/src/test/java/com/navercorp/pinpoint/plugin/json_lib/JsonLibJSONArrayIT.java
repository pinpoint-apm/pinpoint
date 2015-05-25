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

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.BlockType;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.JvmVersion;

import net.sf.json.JSONSerializer;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import net.sf.json.JSON;

/**
 *@author Sangyoon Lee
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("target/pinpoint-agent-1.5.0-SNAPSHOT")
@Dependency({"log4j:log4j:1.2.17", "net.sf.json-lib:json-lib:jar:jdk15:2.3"})
@JvmVersion({6,7})
public class JsonLibJSONArrayIT {

    @Test
    public void fromObjecttest() throws Exception {
        
        String test = "[{'string':'JSON'}]";

        JSONArray jsn = new JSONArray();
        jsn.fromObject(test);
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);
        
        Method targetMethod = JSONArray.class.getMethod("fromObject", Object.class);

        verifier.verifyApi("JsonLib", targetMethod);
        verifier.verifyTraceBlockCount(0);
    }

    @Test
    public void toArraytest() throws Exception {

        JSONObject obj = new JSONObject(); 
        JSONArray test = new JSONArray();
        test.add(obj);
	
        JSONArray.toArray(test);	

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);
        
        Method targetMethod = JSONArray.class.getMethod("toArray", JSONArray.class);

        verifier.verifyApi("JsonLib", targetMethod);
        verifier.verifyTraceBlockCount(1);
    }
}
