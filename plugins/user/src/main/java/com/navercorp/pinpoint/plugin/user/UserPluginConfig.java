/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.user;

import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * 
 * @author jaehong.kim
 *
 */
public class UserPluginConfig {

    private final List<String> includeList;
    private final List<String> mqClientHandlerMethods;

    public UserPluginConfig(ProfilerConfig src) {
        includeList = split(src.readString("profiler.entrypoint", ""));
        mqClientHandlerMethods = src.readList("profiler.message.queue.client.handler.methods");
    }

    public List<String> getIncludeList() {
        return includeList;
    }

    public List<String> getMqClientHandlerMethods() {
        return mqClientHandlerMethods;
    }

    private List<String> split(String values) {
        if (StringUtils.isEmpty(values)) {
            return Collections.emptyList();
        }

        return StringUtils.tokenizeToStringList(values, ",");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{includeList=");
        builder.append(includeList);
        builder.append("}");
        return builder.toString();
    }
}