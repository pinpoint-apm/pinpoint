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

import com.navercorp.pinpoint.bootstrap.module.Providers;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarFileAnalyzer implements PackageAnalyzer {

    private static final String META_INF = "META-INF/";

    private static final String CLASS_EXTENSION = ".class";

    private static final String SERVICE_LOADER = META_INF + "services/";

    private final JarFile jarFile;
    private final JarEntryFilter filter;
    private final ServiceLoaderEntryFilter serviceLoaderEntryFilter;


    JarFileAnalyzer(JarFile jarFile) {
        this.jarFile = Assert.requireNonNull(jarFile, "jarFile");
        this.filter = new PackageFilter();
        this.serviceLoaderEntryFilter = new DefaultServiceLoaderEntryFilter();
    }

    @Override
    public PackageInfo analyze() {
        Set<String> packageSet = new HashSet<>();
        List<Providers> providesList = new ArrayList<>();

        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();

            final String packageName = this.filter.filter(jarEntry);
            if (packageName != null) {
                packageSet.add(packageName);
            }

            final Providers provides = this.serviceLoaderEntryFilter.filter(jarEntry);
            if (provides != null) {
                providesList.add(provides);
            }
        }
        return new PackageInfo(packageSet, providesList);
    }


    interface ServiceLoaderEntryFilter {
        Providers filter(JarEntry jarEntry);
    }

    class DefaultServiceLoaderEntryFilter implements ServiceLoaderEntryFilter {
        @Override
        public Providers filter(JarEntry jarEntry) {
            final String jarEntryName = jarEntry.getName();
            if (!jarEntryName.startsWith(SERVICE_LOADER)) {
                return null;
            }
            if (jarEntry.isDirectory()) {
                return null;
            }
            if (jarEntryName.indexOf('/', SERVICE_LOADER.length()) != -1) {
                return null;
            }
            try {
                InputStream inputStream = jarFile.getInputStream(jarEntry);

                ServiceDescriptorParser parser = new ServiceDescriptorParser();
                List<String> serviceImplClassName = parser.parse(inputStream);
                String serviceClassName = jarEntryName.substring(SERVICE_LOADER.length());
                return new Providers(serviceClassName, serviceImplClassName);
            } catch (IOException e) {
                throw new IllegalStateException(jarFile.getName() + " File read fail ", e);
            }
        }

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
