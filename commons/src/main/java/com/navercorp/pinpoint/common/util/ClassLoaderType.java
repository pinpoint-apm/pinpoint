/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public enum ClassLoaderType {

    // default
    SYSTEM {
        @Override
        public ClassLoader getClassLoader() {
            return ClassLoaderUtils.SYSTEM_CLASS_LOADER;
        }
    },
    EXTENSION {
        @Override
        public ClassLoader getClassLoader() {
            if (ClassLoaderUtils.EXT_CLASS_LOADER != null) {
                return ClassLoaderUtils.EXT_CLASS_LOADER;
            }

            return SYSTEM.getClassLoader();
        }
    },
    BOOTSTRAP {
        @Override
        public ClassLoader getClassLoader() {
            if (ClassLoaderUtils.BOOT_CLASS_LOADER != null) {
                return ClassLoaderUtils.BOOT_CLASS_LOADER;
            }

            return EXTENSION.getClassLoader();
        }
    };

    private static final Set<ClassLoaderType> CLASS_LOADER_TYPE_SET = EnumSet.allOf(ClassLoaderType.class);

    public static ClassLoaderType getType(String value) {
        for (ClassLoaderType classLoaderType : CLASS_LOADER_TYPE_SET) {
            if (classLoaderType.name().equalsIgnoreCase(value)) {
                return classLoaderType;
            }
        }

        return SYSTEM;
    }

    public abstract ClassLoader getClassLoader();

}
