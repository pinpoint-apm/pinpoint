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

package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.BootstrapPackage;
import com.navercorp.pinpoint.profiler.instrument.scanner.JarFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootstrapCore {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final BootstrapPackage bootstrapPackage;
    private final JarFileRepository bootstrapRepository;
    private final ClassLoader bootstrapClassLoader = Object.class.getClassLoader();


    public BootstrapCore(List<String> bootstrapJarPaths) {
        Assert.requireNonNull(bootstrapJarPaths, "bootstrapJarPaths");

        this.bootstrapRepository = new JarFileRepository(bootstrapJarPaths);
        this.bootstrapPackage = new BootstrapPackage();
    }

    public boolean isBootstrapPackage(String className) {
        return bootstrapPackage.isBootstrapPackage(className);
    }

    public boolean isBootstrapPackageByInternalName(String internalClassName) {
        return bootstrapPackage.isBootstrapPackageByInternalName(internalClassName);
    }

    public InputStream openStream(String internalClassName) {
        return bootstrapRepository.openStream(internalClassName);
    }

    public <T> Class<? extends T> loadClass(String className) {
        try {
            if (isDebug) {
                logger.debug("loadClass:{}", className);
            }
            return (Class<T>) Class.forName(className, false, bootstrapClassLoader);
        } catch (ClassNotFoundException ex) {
            if (isDebug) {
                logger.debug("ClassNotFound {} cl:{}", ex.getMessage(), bootstrapClassLoader);
            }
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
