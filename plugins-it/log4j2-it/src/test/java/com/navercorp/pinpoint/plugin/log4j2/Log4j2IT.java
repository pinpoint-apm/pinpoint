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
import com.navercorp.pinpoint.test.plugin.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-spring-bean-test.config")
@JvmVersion(7)
@Dependency({"org.apache.logging.log4j:log4j-core:[2.0,2.13)"})
@JvmArgument("-DtestLoggerEnable=false")
public class Log4j2IT extends Log4j2TestBase {

    @Test
    public void test() {
        Logger logger = LogManager.getLogger();

        final String location = getLoggerJarLocation(logger);
        System.out.println("Log4j2 jar location:" + location);
        final String testVersion = getTestVersion();
        Assert.assertTrue("test version is not " + getTestVersion(), location.contains("/" + testVersion + "/"));

        logger.error("for log4j2 plugin test");
        Assert.assertNotNull("txId", ThreadContext.get("PtxId"));
        Assert.assertNotNull("spanId", ThreadContext.get("PspanId"));
    }

}
