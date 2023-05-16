/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class SpringWebMvcConfig {

    private final boolean enable;
    private final boolean uriStatEnable;
    private final boolean uriStatUseUserInput;

    public SpringWebMvcConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");
        this.enable = config.readBoolean("profiler.spring.webmvc.enable", true);
        this.uriStatEnable = config.readBoolean("profiler.uri.stat.spring.webmvc.enable", false);
        this.uriStatUseUserInput = config.readBoolean("profiler.uri.stat.spring.webmvc.useuserinput", false);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isUriStatEnable() {
        return uriStatEnable;
    }

    public boolean isUriStatUseUserInput() {
        return uriStatUseUserInput;
    }

    @Override
    public String toString() {
        return "SpringWebMvcConfig{" +
                "enable=" + enable +
                ", uriStatEnable=" + uriStatEnable +
                ", uriStatUseUserInput=" + uriStatUseUserInput +
                '}';
    }
}
