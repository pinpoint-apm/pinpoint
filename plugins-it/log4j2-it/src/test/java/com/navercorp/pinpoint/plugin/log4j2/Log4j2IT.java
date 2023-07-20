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
package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-spring-bean-test.config")
@JvmVersion(8)
@Dependency({"org.apache.logging.log4j:log4j-core:[2.17.1,2.20)", PluginITConstants.VERSION})
@JvmArgument("-DtestLoggerEnable=false")
public class Log4j2IT extends Log4j2TestBase {

    @Test
    public void test() {
        Logger logger = LogManager.getLogger();

        final String location = getLoggerJarLocation(logger);
        System.out.println("Log4j2 jar location:" + location);
        final String testVersion = getTestVersion();
        Assertions.assertTrue(location.contains("/" + testVersion + "/"), "test version is not " + getTestVersion());

        logger.error("for log4j2 plugin test");
        Assertions.assertNotNull(ThreadContext.get("PtxId"), "txId");
        Assertions.assertNotNull(ThreadContext.get("PspanId"), "spanId");
    }

}
