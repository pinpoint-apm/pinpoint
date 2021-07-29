/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * // TODO duplicate BytecodeUtils : com.navercorp.pinpoint.test.util.BytecodeUtils
 */
public final class BytecodeUtils {

    private BytecodeUtils() {
    }

    public static byte[] getClassFile(ClassLoader classLoader, String className) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        Objects.requireNonNull(className, "className");

        final String classInternalName = JavaAssistUtils.javaClassNameToJvmResourceName(className);
        final InputStream is = classLoader.getResourceAsStream(classInternalName);
        if (is == null) {
            throw new RuntimeException("No such class file: " + className);
        }
        try {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException(classInternalName + " class read fail");
        }
    }

}
