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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindVariableService;

import java.util.Map;
import java.util.Objects;

/**
 * duplicate : com.navercorp.pinpoint.profiler.modifier.db.interceptor.BindValueUtils
 * @author emeroad
 */
@Deprecated
public class BindValueUtils {
    private static BindVariableService bindVariableService;

    public static void setBindVariableService(BindVariableService bindVariableService) {
        BindValueUtils.bindVariableService = Objects.requireNonNull(bindVariableService, "bindVariableService");
    }

    @Deprecated
    public static String bindValueToString(final Map<Integer, String> bindValueMap, int limit) {
        return bindVariableService.bindVariableToString(bindValueMap, limit);
    }
}
