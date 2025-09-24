/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.name.IdSourceType;
import com.navercorp.pinpoint.profiler.name.NameVersion;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolver;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolverBuilder;

import java.util.Objects;
import java.util.function.Function;

public class ObjectNameBuilder {

    public ObjectName build(AgentOption agentOption, ProfilerConfig profilerConfig) {
        String version = profilerConfig.readString(NameVersion.KEY, "v1");

        NameVersion nameVersion = NameVersion.getVersion(version);

        return resolveObjectName(agentOption.getAgentArgs()::get, nameVersion);
    }


    private void addResolverProperties(ObjectNameResolverBuilder builder, Function<String, String> agentArgs) {
        builder.addProperties(IdSourceType.SYSTEM, System.getProperties()::getProperty);
        builder.addProperties(IdSourceType.SYSTEM_ENV, System.getenv()::get);
        builder.addProperties(IdSourceType.AGENT_ARGUMENT, agentArgs);
    }

    private ObjectName resolveObjectName(Function<String, String> agentArgs, NameVersion nameVersion) {
        Objects.requireNonNull(nameVersion, "nameVersion");

        ObjectNameResolverBuilder builder = new ObjectNameResolverBuilder();
        addResolverProperties(builder, agentArgs);

        ObjectNameResolver objectNameResolver = builder.build(nameVersion);
        return objectNameResolver.resolve();
    }

}
