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

/**
 * @author Taejin Koo
 */
public class SpringWebMvcConfig {

    private final boolean uriStatEnable;

    public SpringWebMvcConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config");
        }

        this.uriStatEnable = config.readBoolean("profiler.spring.webmvc.uri.stat.enable", false);
    }

    public boolean isUriStatEnable() {
        return uriStatEnable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpringWebMvcConfig{");
        sb.append("uriStatEnable=").append(uriStatEnable);
        sb.append('}');
        return sb.toString();
    }
}
