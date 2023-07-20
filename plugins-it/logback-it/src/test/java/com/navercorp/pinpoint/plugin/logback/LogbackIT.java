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
package com.navercorp.pinpoint.plugin.logback;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.StdoutRecorder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@PinpointAgent(AgentPath.PATH)
@Dependency({"ch.qos.logback:logback-classic:[1.0.13],[1.1.0,1.1.11],[1.2.0,1.2.6]", "org.slf4j:slf4j-api:1.7.12", PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-logback-plugin"})
@PinpointConfig("pinpoint-spring-bean-test.config")
@JvmArgument("-DtestLoggerEnable=false")
public class LogbackIT {

    @Test
    public void test() {
        //Logger logger = LogManager.getLogger(getClass());
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error("maru");

        checkVersion(logger);

        Assertions.assertNotNull(MDC.get("PtxId"), "txId");
        Assertions.assertNotNull(MDC.get("PspanId"), "spanId");
    }

    private Logger logger;

    @Test
    public void patternUpdate() {

        final String msg = "pattern";
        StdoutRecorder stdoutRecorder = new StdoutRecorder();

        String log = stdoutRecorder.record(new Runnable() {
            @Override
            public void run() {
                logger = LoggerFactory.getLogger("patternUpdateLogback");
                logger.error(msg);
            }
        });

        System.out.println(log);
        Assertions.assertNotNull(log, "log null");
        Assertions.assertTrue(log.contains(msg), "contains msg");
        Assertions.assertTrue(log.contains("TxId"), "contains TxId");

        Assertions.assertNotNull(logger, "logger null");
        checkVersion(logger);
    }

    private void checkVersion(Logger logger) {
        final String location = getLoggerJarLocation(logger);
        Assertions.assertNotNull(location, "location null");
        System.out.println("Logback classic jar location:" + location);

        final String testVersion = getTestVersion();
        Assertions.assertTrue(location.contains("/" + testVersion + "/"), "test version is not " + getTestVersion());
    }

    private String getTestVersion() {
        final String[] threadInfo = Thread.currentThread().getName()
                .replace(getClass().getName(), "")
                .replace(" Thread", "")
                .replace(" ", "").replace("logback-classic-", "").split(":");
        return threadInfo[0];
    }

    private String getLoggerJarLocation(Object object) {
        return object.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }

}
