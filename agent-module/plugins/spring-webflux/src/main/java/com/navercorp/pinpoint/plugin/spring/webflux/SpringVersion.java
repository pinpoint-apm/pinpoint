package com.navercorp.pinpoint.plugin.spring.webflux;/*
 * Copyright 2024 NAVER Corp.
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

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class SpringVersion {

    public static final int SPRING_VERSION_UNKNOWN = -1;
    public static final int SPRING_VERSION_5 = 5_00_00;
    public static final int SPRING_VERSION_6 = 6_00_00;

    static final String SPRING5_HTTP_STATUS_INTERFACE_NAME = "org.springframework.http.HttpStatus";
    static final String SPRING6_HTTP_STATUS_INTERFACE_NAME = "org.springframework.http.HttpStatusCode";


    public static int getVersion(ClassLoader classLoader) {
        // Spring 6.0 + (boot 3.0 + )
        final Class<?> httpStatusCode = getClass(classLoader, SPRING6_HTTP_STATUS_INTERFACE_NAME);
        if (httpStatusCode != null) {
            return SpringVersion.SPRING_VERSION_6;
        }

        // ~ Spring 5.x (boot 2.0 -)
        final Class<?> httpStatus = getClass(classLoader, SPRING5_HTTP_STATUS_INTERFACE_NAME);
        if (httpStatus != null) {
            return SpringVersion.SPRING_VERSION_5;
        }
        return SpringVersion.SPRING_VERSION_UNKNOWN;
    }


    static Class<?> getClass(ClassLoader classLoader, String className) {
        Objects.requireNonNull(className, "className");
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
