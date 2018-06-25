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

package com.navercorp.pinpoint.bootstrap.java9.module;

import com.navercorp.pinpoint.common.util.ClassUtils;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarPackageAnalyzer implements PackageAnalyzer {

    private static final String META_INF = "META-INF/";
    private static final String CLASS_EXTENSION = ".class";

    private final JarFile jarFile;
    private final JarEntryFilter filter;

    public JarPackageAnalyzer(JarFile jarFile) {
        if (jarFile == null) {
            throw new NullPointerException("jarFile must not be null");
        }
        this.jarFile = jarFile;
        this.filter = new PackageFilter();
    }

    @Override
    public Set<String> getPackage() {

        final Set<String> packageSet = new HashSet<>();

        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();

            final String packageName = this.filter.filter(jarEntry);
            if (packageName == null) {
                continue;
            }

            packageSet.add(packageName);
        }

        return packageSet;
    }



    interface JarEntryFilter {
        String filter(JarEntry jarEntry);
    }

    static class PackageFilter implements JarEntryFilter {
        public String filter(JarEntry jarEntry) {
            if (jarEntry.getName().startsWith(META_INF)) {
                // skip META-INF
                return null;
            }
            if (jarEntry.isDirectory()) {
                // skip empty dir
                return null;
            }

            final String fileName = jarEntry.getName();
            if (!checkFIleExtension(fileName, CLASS_EXTENSION)) {
                // skip non class file
                return null;
            }

            final String packageName = ClassUtils.getPackageName(fileName, '/', null);
            if (packageName == null) {
                return null;
            }
            return toPackageName(packageName);
        }

        private boolean checkFIleExtension(String fileName, String extension) {
            return fileName.endsWith(extension);
        }


        private String toPackageName(String dirFormat) {
            if (dirFormat == null) {
                return null;
            }
            return dirFormat.replace('/', '.');
        }
    }
}
