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
package com.navercorp.pinpoint.it.plugin.log4j;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import com.navercorp.pinpoint.test.plugin.TransformInclude;
import org.junit.jupiter.api.Test;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"log4j:log4j:[1.2.16,)", PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-log4j-plugin"})
@PinpointConfig("pinpoint-spring-bean-test.config")
@TransformInclude("org.apache.log4j.")
public class Log4jIT extends Log4jTestBase {

    @Test
    public void test() {
        checkMDC();
    }

    @Test
    public void patternUpdate() {
        checkPatternReplace();
    }
}
