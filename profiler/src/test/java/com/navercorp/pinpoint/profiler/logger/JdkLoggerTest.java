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

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public class JdkLoggerTest {
    @Test
    public void test() {
        Logger logger = Logger.getLogger(this.getClass().getName());

        logger.info("tset");
        // format은 역시 안되네.
        logger.log(Level.INFO, "Test %s", "sdfsdf");

        logger.log(Level.INFO, "Test ", new Exception());

        logger.logp(Level.INFO, JdkLoggerTest.class.getName(), "test()", "tsdd");

        // logging.properties 에 fine으로 되어 있으므로 출력안되야 됨.
        logger.finest("로그가 나오는지?");

    }
}
