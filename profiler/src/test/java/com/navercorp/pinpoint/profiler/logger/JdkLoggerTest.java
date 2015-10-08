/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.logger;

import org.junit.Ignore;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public class JdkLoggerTest {
    @Ignore
    @Test
    public void test() {
        Logger logger = Logger.getLogger(this.getClass().getName());

        logger.info("tset");

        // formatting is not supported
        logger.log(Level.INFO, "Test %s", "sdfsdf");

        logger.log(Level.INFO, "Test ", new Exception());

        logger.logp(Level.INFO, JdkLoggerTest.class.getName(), "test()", "tsdd");

        // Should not log this because log level for test defined in logging.properties is "fine".
        logger.finest("logged?");

    }
}
