/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.apache.dubbo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author K
 */
public class ApacheDubboConfiguration {

    private final boolean dubboEnabled;

    private final List<String> dubboBootstrapMains;

    public ApacheDubboConfiguration(ProfilerConfig config) {
        this.dubboEnabled = config.readBoolean("profiler.apache.dubbo.enable", true);
        this.dubboBootstrapMains = config.readList("profiler.apache.dubbo.bootstrap.main");
    }

    public boolean isDubboEnabled() {
        return dubboEnabled;
    }

    public List<String> getDubboBootstrapMains() {
        return dubboBootstrapMains;
    }

    @Override
    public String toString() {
        return "ApacheDubboConfiguration{" +
                "dubboEnabled=" + dubboEnabled +
                ", dubboBootstrapMains=" + dubboBootstrapMains +
                '}';
    }
}
