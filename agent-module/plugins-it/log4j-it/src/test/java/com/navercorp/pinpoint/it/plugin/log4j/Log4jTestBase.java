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

import com.navercorp.pinpoint.it.plugin.utils.StdoutRecorder;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.junit.jupiter.api.Assertions;

public class Log4jTestBase {

    private Logger logger;

    protected void checkMDC() {
        Logger logger = Logger.getLogger(getClass());
        logger.error("maru");

        checkVersion(logger);
        Object ptxId = MDC.get("PtxId");
        Assertions.assertNotNull(ptxId, "TxId");
        Assertions.assertInstanceOf(String.class, ptxId, "TxId type");
        String id = (String) ptxId;
        Assertions.assertTrue(id.contains("build.test.0^1"), "TxId value");
        Assertions.assertNotNull(MDC.get("PspanId"), "spanId");
    }

    protected String checkPatternReplace() {
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
        Assertions.assertTrue(log.contains("build.test.0^1"), "contains TxId value");

        Assertions.assertNotNull(logger, "logger null");
        checkVersion(logger);
        return log;
    }

    private void checkVersion(Logger logger) {
        final String location = getLoggerJarLocation(logger);
        Assertions.assertNotNull(location, "location null");
        System.out.println("Log4j jar location:" + location);

        final String testVersion = getTestVersion();
        Assertions.assertTrue(location.contains("/" + testVersion + "/"), "test version is not " + getTestVersion());
    }

    private String getTestVersion() {
        final String[] threadInfo = Thread.currentThread().getName()
                .replace(getClass().getName(), "")
                .replace(" Thread", "")
                .replace(" ", "").replace("log4j-", "").split(":");
        return threadInfo[0];
    }

    private String getLoggerJarLocation(Object object) {
        return object.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }

}
