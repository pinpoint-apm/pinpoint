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

import com.navercorp.pinpoint.bootstrap.java9.module.merger.JarMerger;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
class ModuleBuilder {

    private final ModuleLogger logger = ModuleLogger.getLogger(getClass().getName());

    Module defineModule(String moduleName, ClassLoader classLoader, URL[] urls) {
        Objects.requireNonNull(moduleName, "moduleName");
        Objects.requireNonNull(urls, "urls");
        if (urls.length == 0) {
            throw new IllegalArgumentException("urls.length is 0");
        }
        logger.info("bootstrap unnamedModule:" +  InternalModules.getUnnamedModule());
        logger.info("platform unnamedModule:" + ClassLoader.getPlatformClassLoader().getUnnamedModule());
        logger.info("system unnamedModule:" + ClassLoader.getSystemClassLoader().getUnnamedModule());

        Module unnamedModule = classLoader.getUnnamedModule();
        logger.info("defineModule classLoader: " + classLoader);
        logger.info("defineModule classLoader-unnamedModule: " + unnamedModule);

        final List<URI> uris = new ArrayList<>(urls.length);
        for (final URL url: urls) {
            try {
                uris.add(url.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid lib url", e);
            }
        }

        final ModuleLayer bootLayer = ModuleLayer.boot();

        try {
            final ModuleFinder finder = JarMerger.build(moduleName, uris);
            final Configuration config = bootLayer.configuration().resolve(finder, ModuleFinder.of(), Set.of(moduleName));
            final Module module = bootLayer.defineModules(config, name -> classLoader)
                    .modules()
                    .iterator()
                    .next();

            logger.info("defineModule module:" + module);
            return module;
        } catch (IOException e) {
            throw new RuntimeException("Failed to define module", e);
        }
    }

}
