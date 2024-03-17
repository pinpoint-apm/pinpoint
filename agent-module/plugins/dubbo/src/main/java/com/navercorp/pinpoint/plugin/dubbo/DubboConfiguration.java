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

package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class DubboConfiguration {

    private final boolean dubboEnabled;
    private final List<String> dubboBootstrapMains;

    public DubboConfiguration(ProfilerConfig config) {
        this.dubboEnabled = config.readBoolean("profiler.dubbo.enable", true);
        this.dubboBootstrapMains = config.readList("profiler.dubbo.bootstrap.main");
    }

    public boolean isDubboEnabled() {
        return dubboEnabled;
    }

    public List<String> getDubboBootstrapMains() {
        return dubboBootstrapMains;
    }

    @Override
    public String toString() {
        return "DubboConfiguration{" +
                "dubboEnabled=" + dubboEnabled +
                ", dubboBootstrapMains=" + dubboBootstrapMains +
                '}';
    }
}
