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
package com.navercorp.pinpoint.plugin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.fasterxml.jackson.core:jackson-databind:[2.6.1]"})
@PinpointConfig("pinpoint-disabled-plugin-test.config")
public class PluginDisableIT {

    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "jackson");
        
        mapper.writeValueAsString(map);
        mapper.writeValueAsBytes(map);
        
        ObjectWriter writer = mapper.writer();
        
        writer.writeValueAsString(map);
        writer.writeValueAsBytes(map);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTraceCount(0);
    }
}


