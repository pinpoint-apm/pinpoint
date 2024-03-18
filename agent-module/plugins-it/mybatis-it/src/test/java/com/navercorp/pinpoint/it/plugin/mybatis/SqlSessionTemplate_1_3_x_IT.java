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

package com.navercorp.pinpoint.it.plugin.mybatis;


import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;

/**
 * Tests against mybatis-spring 1.3.x (1.3.x requires mybatis 3.4.0 or higher)
 *
 * @author HyunGil Jeong
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@Dependency({ "org.mybatis:mybatis-spring:[1.3.0,1.3.max)", "org.mybatis:mybatis:3.4.0",
        "org.springframework:spring-jdbc:[4.1.7.RELEASE]", "org.mockito:mockito-core:4.8.1" })
@ImportPlugin("com.navercorp.pinpoint:pinpoint-mybatis-plugin")
public class SqlSessionTemplate_1_3_x_IT extends SqlSessionTemplateITBase {
}
