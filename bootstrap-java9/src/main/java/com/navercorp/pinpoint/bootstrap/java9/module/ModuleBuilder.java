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

import jdk.internal.loader.BootLoader;
import jdk.internal.module.Modules;

import java.io.Closeable;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
class ModuleBuilder {

    private final ModuleLogger logger = ModuleLogger.getLogger(getClass().getName());

    Module defineModule(String moduleName, ClassLoader classLoader, URL[] urls) {
        if (moduleName == null) {
            throw new NullPointerException("moduleName must not be null");
        }
        if (urls == null) {
            throw new NullPointerException("urls must not be null");
        }
        if (urls.length == 0) {
            throw new IllegalArgumentException("urls.length is 0");
        }
        logger.info("boot " +  BootLoader.getUnnamedModule());
        logger.info("platform "+ ClassLoader.getPlatformClassLoader().getUnnamedModule());
        logger.info("system "+ ClassLoader.getSystemClassLoader().getUnnamedModule());

        Module unnamedModule = classLoader.getUnnamedModule();
        logger.info("defineModule classLoader: " + classLoader);
        logger.info("defineModule classLoader-unnamed: " + unnamedModule);


        Set<String> packages = getPackages(urls);
        logger.info("packages:" + packages);

        ModuleDescriptor.Builder builder = ModuleDescriptor.newModule(moduleName);
        builder.packages(packages);

        ModuleDescriptor moduleDescriptor = builder.build();
        URI url = getInformationURI(urls);

        Module module = Modules.defineModule(classLoader, moduleDescriptor , url);
        logger.info("defineModule module:" + module);
        return module;
    }

    private JarFile newJarFile(URL jarFile) {
        try {
            if (!jarFile.getProtocol().equals("file")) {
                throw new IllegalStateException("invalid file " + jarFile);
            }
            return new JarFile(jarFile.getFile());
        } catch (IOException e) {
            throw new ModuleException(jarFile.getFile() +  " create fail " + e.getMessage(), e);
        }
    }

    private URI getInformationURI(URL[] urls) {
        if (isEmpty(urls)) {
            return null;
        }
        final URL url = urls[0];
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isEmpty(URL[] urls) {
        return urls == null || urls.length == 0;
    }

    private Set<String> getPackages(URL[] urls) {

        final Set<String> sum = new HashSet<>();
        for (URL url : urls) {
            if (!isJar(url)) {
                continue;
            }
            JarFile jarFile = null;
            try {
                jarFile = newJarFile(url);
                PackageAnalyzer packageAnalyzer = new JarPackageAnalyzer(jarFile);
                Set<String> newPackage = packageAnalyzer.getPackage();
                sum.addAll(newPackage);
            } finally {
                close(jarFile);
            }
        }
        return sum;
    }

    private boolean isJar(URL url){
         // filter *.xml
        if (url.getPath().endsWith(".jar")) {
            return true;
        }
        return false;
    }

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignore) {
            // skip
        }
    }

}
