/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.config;

/**
 * How the application type is resolved when {@code profiler.applicationservertype} is not set.
 */
public enum ApplicationServerTypeMode {
    /**
     * No service name (uid v1, v3): agent plugins (Tomcat, Jetty, ...) detect the type.
     */
    V1,
    /**
     * Service name (uid v4): report the single type JAVA for a named service,
     * like the agents of other languages report their language.
     * The DEFAULT service keeps the {@link #V1} rule.
     * Default since 4.0.0.
     */
    V4;

    public static final ApplicationServerTypeMode DEFAULT = V4;

    public static ApplicationServerTypeMode parse(String value) {
        if (value == null) {
            return DEFAULT;
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT;
        }
        return valueOf(trimmed.toUpperCase());
    }
}
