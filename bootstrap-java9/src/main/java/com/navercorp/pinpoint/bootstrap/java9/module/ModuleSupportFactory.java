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

package com.navercorp.pinpoint.bootstrap.java9.module;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ModuleSupportFactory {
    public ModuleSupportFactory() {
    }

    public ModuleSupport newModuleSupport(Instrumentation instrumentation) {
        // Dynamic changes are required?
        // move to pinpoint.config?
        List<String> allowedProviders = Arrays.asList(
                "io.grpc.NameResolverProvider",
                "com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestMetadataProvider",
                "com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParserProvider",
                "io.grpc.ManagedChannelProvider"
        );

        return new ModuleSupport(instrumentation, allowedProviders);
    }
}
