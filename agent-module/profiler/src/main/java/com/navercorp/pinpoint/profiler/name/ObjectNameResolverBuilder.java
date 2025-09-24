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

package com.navercorp.pinpoint.profiler.name;

import com.navercorp.pinpoint.profiler.name.v1.IdValidatorV1;
import com.navercorp.pinpoint.profiler.name.v1.ObjectNameResolverV1;
import com.navercorp.pinpoint.profiler.name.v4.ObjectNameResolverV4;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectNameResolverBuilder {
    private final List<AgentProperties> agentProperties = new ArrayList<>();
    

    public void addProperties(IdSourceType sourceType, Function<String, String> env) {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(env, "env");

        AgentProperties agentProperties = new AgentProperties(sourceType, env);
        this.agentProperties.add(agentProperties);
    }

    public ObjectNameResolver build() {
        return buildV1();
    }

    public ObjectNameResolver build(NameVersion version) {
        Objects.requireNonNull(version, "version");

        switch (version) {
            case v1:
                return buildV1();
            case v3:
                return buildV3();
            case v4:
                return buildV4();
            default:
                return buildV1();
        }
    }

    public ObjectNameResolver buildV1() {
        List<AgentProperties> copy = new ArrayList<>(this.agentProperties);
        return new ObjectNameResolverV1(copy);
    }

    public ObjectNameResolver buildV3() {
        List<AgentProperties> copy = new ArrayList<>(this.agentProperties);
        IdValidator idValidator = IdValidatorV1.v3();
        return new ObjectNameResolverV1(idValidator, copy);
    }

    public ObjectNameResolver buildV4() {
        List<AgentProperties> copy = new ArrayList<>(this.agentProperties);
        return new ObjectNameResolverV4(copy);
    }
}
