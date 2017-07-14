/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.spring.boot;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class SpringBootConfiguration {

    private final boolean springBootEnabled;
    private final List<String> springBootBootstrapMains;

    public SpringBootConfiguration(ProfilerConfig config) {
        this.springBootEnabled = config.readBoolean("profiler.springboot.enable", true);
        this.springBootBootstrapMains = config.readList("profiler.springboot.bootstrap.main");
    }

    public boolean isSpringBootEnabled() {
        return springBootEnabled;
    }

    public List<String> getSpringBootBootstrapMains() {
        return springBootBootstrapMains;
    }
}
