/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.redis.redisson;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jaehong.kim
 */
public class RedissonMethodNameFilters {

    private static final Set<String> DEFAULT_EXCLUDE_METHOD_NAMES = new HashSet<String>(Arrays.asList("clone", "equals", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait"));

    private RedissonMethodNameFilters() {
    }

    public static MethodFilter exclude(String... methodNames) {
        return new DefaultMethodFilter(methodNames);
    }

    private static final class DefaultMethodFilter implements MethodFilter {
        private final Set<String> excludeMethodNames = new HashSet<String>();

        public DefaultMethodFilter(final String... methodNames) {
            this.excludeMethodNames.addAll(Arrays.asList(methodNames));
        }

        @Override
        public boolean accept(InstrumentMethod method) {
            if (method != null) {
                final int modifiers = method.getModifiers();
                // Only public.
                if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
                    return false;
                }
                final String name = method.getName();
                // Skip pinpoint and java.lang.Object methods.
                if (!name.startsWith("_$PINPOINT$_") && !DEFAULT_EXCLUDE_METHOD_NAMES.contains(name) && !this.excludeMethodNames.contains(name)) {
                    return true;
                }
            }
            return false;
        }
    }
}