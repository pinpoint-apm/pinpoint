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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
    private final Function<JarEntry, String> filter;
    private final Function<JarEntry, Providers> serviceLoaderEntryFilter;


    JarFileAnalyzer(JarFile jarFile) {
        this.jarFile = Objects.requireNonNull(jarFile, "jarFile");
        this.filter = new PackageFilter();
        this.serviceLoaderEntryFilter = new ServiceLoaderEntryFilter(jarFile);
    }

    @Override
    public PackageInfo analyze() {
        Set<String> packageSet = new HashSet<>();
        List<Providers> providesList = new ArrayList<>();

        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();

            final String packageName = this.filter.apply(jarEntry);
            if (packageName != null) {
                packageSet.add(packageName);
            }

            final Providers provides = this.serviceLoaderEntryFilter.apply(jarEntry);
            if (provides != null) {
                packageSet.add(provides.getServicePackage());
                providesList.add(provides);
            }
        }
        return new PackageInfo(packageSet, providesList);
    }


    static class ServiceLoaderEntryFilter implements Function<JarEntry, Providers> {

        private final JarFile jarFile;

        public ServiceLoaderEntryFilter(JarFile jarFile) {
            this.jarFile = Objects.requireNonNull(jarFile, "jarFile");
        }

        public Providers apply(JarEntry jarEntry) {
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

    static class PackageFilter implements Function<JarEntry, String> {
        public String apply(JarEntry jarEntry) {
            if (jarEntry.getName().startsWith(META_INF)) {
                // skip META-INF
                return null;
            }
            if (jarEntry.isDirectory()) {
                // skip empty dir
                return null;
            }

            final String fileName = jarEntry.getName();
            if (!checkFileExtension(fileName, CLASS_EXTENSION)) {
                // skip non-class file
                return null;
            }

            final String packageName = ModuleUtils.getPackageName(fileName, '/');
            if (packageName == null) {
                return null;
            }
            return ModuleUtils.toPackageName(packageName);
        }

        private boolean checkFileExtension(String fileName, String extension) {
            return fileName.endsWith(extension);
        }
    }
}
