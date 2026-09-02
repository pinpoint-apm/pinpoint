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
package com.navercorp.pinpoint.it.plugin.spring.web;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Test;


@PluginTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({"org.springframework:spring-webmvc:[5.2.0.RELEASE,5.2.25.RELEASE]", "org.springframework:spring-test", "javax.servlet:javax.servlet-api:4.0.1"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-plugin"})
public class SpringWebMvc_5_2_IT extends SpringWebMvc_5_1_IT {

    @Test
    public void testRequest() throws Exception {
        super.testRequest();
    }
}
