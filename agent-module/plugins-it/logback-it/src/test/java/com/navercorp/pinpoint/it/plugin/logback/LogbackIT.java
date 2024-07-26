/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.logback;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import com.navercorp.pinpoint.test.plugin.TransformInclude;
import org.junit.jupiter.api.Test;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"ch.qos.logback:logback-classic:[1.0.13],[1.1.0,1.1.11],[1.2.0,1.2.6]", "org.slf4j:slf4j-api:1.7.12", PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-logback-plugin"})
@PinpointConfig("pinpoint-spring-bean-test.config")
@JvmArgument("-DtestLoggerEnable=false")
@TransformInclude("org.slf4j.")
public class LogbackIT extends LogbackTestBase {

    @Test
    public void test() {
        checkMDC();
    }

    @Test
    public void patternUpdate() {
        checkPatternUpdate();
    }

}
