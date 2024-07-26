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
package com.navercorp.pinpoint.it.plugin.log4j2;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-spring-bean-test.config")
@JvmVersion(11)
@Dependency({"org.apache.logging.log4j:log4j-core:[2.20,2.22]", PluginITConstants.VERSION})
@JvmArgument({"-DtestLoggerEnable=false", "-Dprofiler.log4j2.logging.pattern.full_replace.with=Log4j2IT TxId:%X{PtxId} %message"})
public class Log4J2PatternFullReplace_2_20_ITTest extends Log4j2PatternTestBase {

    @Test
    public void patternUpdate() {
        String log = checkPatternUpdate();
        Assertions.assertTrue(log.contains("Log4j2IT"), "contains full-replace string Log4j2IT");
    }

}
