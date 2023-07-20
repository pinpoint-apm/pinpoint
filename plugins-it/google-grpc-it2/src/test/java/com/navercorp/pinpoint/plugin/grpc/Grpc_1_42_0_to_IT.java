/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;

/**
 * @author Taejin Koo
 */
@PinpointAgent(AgentPath.PATH)
@Dependency({"io.grpc:grpc-stub:[1.42.0,1.43.max]", "io.grpc:grpc-netty:[1.23.0]", "io.grpc:grpc-protobuf:[1.23.0]",
        PluginITConstants.VERSION})
@ImportPlugin("com.navercorp.pinpoint:pinpoint-grpc-plugin")
@PinpointConfig("pinpoint-grpc-plugin-test.config")
public class Grpc_1_42_0_to_IT extends GrpcITBase {

    @Override
    protected int getExpectedRequestResponseTestTraceCount() {
        return 11;
    }

    @Override
    protected int getExpectedStreamingTestInitializationCount() {
        return 8;
    }
}
