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
import com.navercorp.pinpoint.it.plugin.utils.StdoutRecorder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import com.navercorp.pinpoint.test.plugin.TransformInclude;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"log4j:log4j:[1.2.16,)", PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-log4j-plugin"})
@PinpointConfig("pinpoint-spring-bean-test.config")
@TransformInclude("org.apache.log4j.")
public class Log4jIT {

    private Logger logger;

    @Test
    public void test() {
        Logger logger = Logger.getLogger(getClass());
        logger.error("maru");

        Log4jTestUtils.checkVersion(logger, this);

        Assertions.assertNotNull(MDC.get("PtxId"), "txId");
        Assertions.assertNotNull(MDC.get("PspanId"), "spanId");
    }

    @Test
    public void patternUpdate() {
        final String msg = "pattern";


        StdoutRecorder stdoutRecorder = new StdoutRecorder();
        final String log = stdoutRecorder.record(new Runnable() {
            @Override
            public void run() {
                logger = Logger.getLogger("patternUpdateLogback");
                logger.error(msg);
            }
        });

        System.out.println(log);
        Assertions.assertNotNull(log, "log null");
        Assertions.assertTrue(log.contains(msg), "contains msg");
        Assertions.assertTrue(log.contains("TxId"), "contains TxId");

        Assertions.assertNotNull(logger, "logger null");
        Log4jTestUtils.checkVersion(logger, this);
    }



}
