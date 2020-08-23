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

package com.navercorp.pinpoint.profiler.transformer;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class UnmodifiableClassFilter implements ClassFileFilter {
    private static final Object PRESENT = new Object();

    private final Map<String, Object> allowJdkClassNames;

    public UnmodifiableClassFilter() {
        this(Collections.<String>emptyList());
    }
    public UnmodifiableClassFilter(List<String> allowJdkClassNames) {
        this.allowJdkClassNames = newJdkClassNameMap(allowJdkClassNames);
    }

    private Map<String, Object> newJdkClassNameMap(List<String> allowJdkClassNames) {
        Map<String, Object> allowJdkClass = new HashMap<String, Object>();
        for (String allowJdkClassName : allowJdkClassNames) {
            String jvmName = JavaAssistUtils.javaNameToJvmName(allowJdkClassName);
            allowJdkClass.put(jvmName, PRESENT);
        }
        return allowJdkClass;
    }

    @Override
    public boolean accept(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (className == null) {
            return SKIP;
        }

        // fast skip java classes
        if (className.startsWith("java")) {
            if (className.startsWith("/", 4) || className.startsWith("x/", 4)) {
                if (allowJdkClassName(className)) {
                    return CONTINUE;
                }
                return SKIP;
            }
        }

        return CONTINUE;
    }

    private boolean allowJdkClassName(String className) {
        return allowJdkClassNames.containsKey(className);
    }
}