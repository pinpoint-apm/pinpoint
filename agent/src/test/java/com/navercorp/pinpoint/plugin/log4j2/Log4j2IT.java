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
package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PinpointPluginTestSuite.class)
@Dependency({"org.apache.logging.log4j:log4j-core:[2.10.0]", "org.apache.logging.log4j:log4j-api:[2.10.0]"})
@PinpointConfig("pinpoint-spring-bean-test.config")
public class Log4j2IT {

    @Test
    public void test() throws Exception {
        Logger logger = Logger.getLogger(getClass());
        logger.error("maru");
        
        Assert.assertNotNull(MDC.get("PtxId"));
        Assert.assertNotNull(MDC.get("PspanId"));
    }
}
