/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.util;


import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class JarFileUtils {

    private JarFileUtils() {
    }

    public static String getManifestValue(JarFile jarFile, String key, String defaultValue) {
        final Manifest manifest = getManifest(jarFile);
        if (manifest == null) {
            return defaultValue;
        }

        final Attributes attributes = manifest.getMainAttributes();
        final String value = attributes.getValue(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }


    public static Manifest getManifest(JarFile pluginJarFile) {
        try {
            return pluginJarFile.getManifest();
        } catch (IOException ex) {
            return null;
        }
    }
}
