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
package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-spring-bean-test.config")
@JvmVersion(7)
@Dependency({"org.apache.logging.log4j:log4j-core:[2.8,2.13)"})
@JvmArgument("-DtestLoggerEnable=false")
public class Log4j2PatternIT extends Log4j2TestBase {

    @Test
    public void patternUpdate() throws IOException {
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final String msg = "pattern";

        String location;
        try {
            System.setOut(new PrintStream(outputStream));
            Logger logger = LogManager.getLogger("patternUpdateLog4j2Jvm7");
            logger.error(msg);
            location = getLoggerJarLocation(logger);
        } finally {
            System.setOut(originalOut);
        }

        final String log = outputStream.toString();
        outputStream.close();
        System.out.println(log);
        Assert.assertNotNull("log null", log);
        Assert.assertTrue("contains msg", log.contains(msg));
        Assert.assertTrue("contains TxId", log.contains("TxId"));

        Assert.assertNotNull("location null", location);
        System.out.println("Log4j2 jar location:" + location);
        final String testVersion = getTestVersion();
        Assert.assertTrue("test version is not " + getTestVersion(), location.contains("/" + testVersion + "/"));
    }

}
