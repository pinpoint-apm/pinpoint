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

package com.navercorp.pinpoint.bootstrap.util;

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;

import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class PlatformClassLoaderUtils {

    private static ClassLoader platformClassLoader = lookupPlatformOrBootstrapClassLoader();

    private PlatformClassLoaderUtils() {
    }

    private static ClassLoader lookupPlatformOrBootstrapClassLoader() {
        if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9)) {
            return getPlatformClassLoader0();
        } else {
            // java 8 under
            return Object.class.getClassLoader();
        }
    }

    private static ClassLoader getPlatformClassLoader0() {
//        Warning : Not recommended
//        return ClassLoader.getSystemClassLoader().getParent();
        try {
            // java9 : ClassLoader.getPlatformClassLoader()
            Method getPlatformClassLoader = ClassLoader.class.getDeclaredMethod("getPlatformClassLoader");
            return (ClassLoader) getPlatformClassLoader.invoke(ClassLoader.class);
        } catch (Exception ex) {
            throw new IllegalStateException("ClassLoader.getPlatformClassLoader() invoke fail Caused by:" + ex.getMessage(), ex);
        }
    }

    public static ClassLoader getPlatformOrBootstrapClassLoader() {
        return platformClassLoader;
    }


    public static Class<?> findClassFromPlatformClassLoader(String className) {
        try {
            return Class.forName(className, false, platformClassLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
