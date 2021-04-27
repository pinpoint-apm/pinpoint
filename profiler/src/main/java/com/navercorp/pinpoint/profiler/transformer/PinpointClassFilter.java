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

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class PinpointClassFilter implements ClassFileFilter {

    public static final String DEFAULT_PACKAGE = "com/navercorp/pinpoint/";
    public static final List<String> DEFAULT_EXCLUDES = Arrays.asList("web/", "sdk/");

    private final String basePackage;
    private final String[] excludes;

    public PinpointClassFilter() {
        this(DEFAULT_PACKAGE, DEFAULT_EXCLUDES);
    }

    public PinpointClassFilter(String basePackage, List<String> excludes) {
        this.basePackage = Objects.requireNonNull(basePackage, "basePackage");
        Objects.requireNonNull(excludes, "excludes");
        this.excludes = excludes.toArray(new String[0]);
    }

    @Override
    public boolean accept(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (className == null) {
            return SKIP;
        }

        // Skip pinpoint packages too.
        if (className.startsWith(basePackage)) {
            for (String exclude : excludes) {
                if (className.startsWith(exclude, basePackage.length())) {
                    return CONTINUE;
                }
            }
            return SKIP;
        }

        return CONTINUE;
    }

    @Override
    public String toString() {
        return "PinpointClassFilter{" +
                "basePackage='" + basePackage + '\'' +
                ", excludes=" + Arrays.toString(excludes) +
                '}';
    }
}
