/*
 * Copyright 2021 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.log4j2;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import org.junit.jupiter.api.Test;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-spring-bean-test.config")
@JvmVersion(8)
@Dependency({"org.apache.logging.log4j:log4j-core:[2.17.1,2.20)", PluginITConstants.VERSION})
@JvmArgument("-DtestLoggerEnable=false")
public class Log4J2PatternTestIT extends Log4j2PatternTestBase {

    @Test
    public void patternUpdate() {
        checkPatternUpdate();
    }

}
