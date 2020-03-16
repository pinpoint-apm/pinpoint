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

package com.navercorp.pinpoint.plugin.mybatis;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.runner.RunWith;

/**
 * Tests against mybatis-spring 1.1.x (1.1.x requires mybatis 3.1.0 or higher)
 * Prior versions do not handle mocked SqlSession proxies well.
 *
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({ "org.mybatis:mybatis-spring:[1.1.0,1.1.max)", "org.mybatis:mybatis:3.2.7",
        "org.springframework:spring-jdbc:[4.1.7.RELEASE]", "org.mockito:mockito-all:1.8.4" })
@ImportPlugin("com.navercorp.pinpoint:pinpoint-mybatis-plugin")
public class SqlSessionTemplate_1_1_x_IT extends SqlSessionTemplateITBase {
}
