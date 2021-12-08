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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class CoroutinesConfig {

    private final boolean traceCoroutines;
    private final List<String> nameIncludeList;

    public CoroutinesConfig(ProfilerConfig config) {
        this.traceCoroutines = config.readBoolean("profiler.kotlin.coroutines.enable", false);
        String nameIncludes = config.readString("profiler.kotlin.coroutines.name.include", "");

        if (StringUtils.hasLength(nameIncludes)) {
            String[] nameIncludeArray = nameIncludes.split(",");
            List<String> result = new ArrayList<>(nameIncludeArray.length);
            for (String nameInclude : nameIncludeArray) {
                if (StringUtils.hasLength(nameInclude)) {
                    result.add(nameInclude);
                }
            }
            nameIncludeList = Collections.unmodifiableList(result);
        } else {
            nameIncludeList = Collections.emptyList();
        }
    }

    public boolean isTraceCoroutines() {
        return traceCoroutines;
    }

    public List<String> getIncludedNameList() {
        return nameIncludeList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CoroutinesConfig{");
        sb.append("traceCoroutines=").append(traceCoroutines);
        sb.append(", nameIncludeList=").append(nameIncludeList);
        sb.append('}');
        return sb.toString();
    }
}
