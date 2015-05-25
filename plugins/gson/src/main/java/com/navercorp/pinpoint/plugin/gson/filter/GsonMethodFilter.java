/**
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
package com.navercorp.pinpoint.plugin.gson.filter;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author ChaYoung You
 */
public class GsonMethodFilter implements MethodFilter {
    private static final boolean TRACK = false;
    private static final boolean DO_NOT_TRACK = true;
    private final Set<String> methodNames;

    public GsonMethodFilter(final Set<String> methodNames) {
        this.methodNames = methodNames;
    }

    @Override
    public boolean filter(MethodInfo method) {
        final int modifiers = method.getModifiers();

        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
            return DO_NOT_TRACK;
        }

        if (methodNames.contains(method.getName())) {
            return TRACK;
        }

        return DO_NOT_TRACK;
    }
}
