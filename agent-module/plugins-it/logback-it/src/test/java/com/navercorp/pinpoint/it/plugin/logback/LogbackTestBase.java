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

import com.navercorp.pinpoint.it.plugin.utils.StdoutRecorder;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogbackTestBase {

    protected void checkMDC() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error("maru");

        checkVersion(logger);

        String ptxId = MDC.get("PtxId");
        Assertions.assertNotNull(ptxId, "TxId");
        Assertions.assertTrue(ptxId.contains("build.test.0^1"), "TxId value");
        Assertions.assertNotNull(MDC.get("PspanId"), "spanId");
    }

    private Logger logger;

    protected String checkPatternUpdate() {

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
        Assertions.assertTrue(log.contains("build.test.0^1"), "TxId value");

        Assertions.assertNotNull(logger, "logger null");
        checkVersion(logger);
        return log;
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
