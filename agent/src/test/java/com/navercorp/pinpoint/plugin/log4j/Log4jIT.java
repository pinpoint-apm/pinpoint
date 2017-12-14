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
package com.navercorp.pinpoint.plugin.log4j;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

@RunWith(PinpointPluginTestSuite.class)
@Dependency({"log4j:log4j:[1.2.16,)"})
@PinpointConfig("pinpoint-spring-bean-test.config")
public class Log4jIT {

    @Test
    public void test() throws Exception {
        Logger logger = Logger.getLogger(getClass());
        logger.error("maru");
        
        Assert.assertNotNull(MDC.get("PtxId"));
        Assert.assertNotNull(MDC.get("PspanId"));
    }
}
