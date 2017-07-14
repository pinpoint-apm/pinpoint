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
package com.navercorp.pinpoint.plugin.logback;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

@RunWith(PinpointPluginTestSuite.class)
@Dependency({"ch.qos.logback:logback-classic:[1.0.13],[1.1.3,)", "org.slf4j:slf4j-api:1.7.12"})
@PinpointConfig("pinpoint-spring-bean-test.config")
public class LogbackIT {

    @Test
    public void test() throws Exception {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.error("maru");
        
        Assert.assertNotNull(MDC.get("PtxId"));
        Assert.assertNotNull(MDC.get("PspanId"));
    }
}
