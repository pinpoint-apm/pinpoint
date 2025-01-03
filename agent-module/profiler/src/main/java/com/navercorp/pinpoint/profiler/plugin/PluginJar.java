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

package com.navercorp.pinpoint.profiler.plugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;

/**
 * @author HyunGil Jeong
 */
public class PluginJar {

    public static final String PINPOINT_PLUGIN_ID = "Pinpoint-Plugin-Id";
    public static final String PINPOINT_PLUGIN_PACKAGE = "Pinpoint-Plugin-Package";
    public static final String PINPOINT_PLUGIN_COMPILER_VERSION = "Pinpoint-Plugin-Compiler-Version";
    public static final String DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME = "com.navercorp.pinpoint.plugin";
    public static final String PINPOINT_PLUGIN_PACKAGE_CLASS_REQUIREMENTS = "Pinpoint-Plugin-Package-Class-Requirements";

    private final URL url;
    private final JarFile jarFile;
    private final PluginManifest manifest;

    public PluginJar(Path path) {
        Objects.requireNonNull(path, "path");
        this.url = toURL(path);

        this.jarFile = createJarFile(path);
        this.manifest = PluginManifest.of(jarFile);
    }


    private URL toURL(Path file) {
        try {
            return file.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new PluginException(file + " toURL error", e);
        }
    }

    private static JarFile createJarFile(Path pluginJar) {
        try {
            verify(pluginJar);
            return new JarFile(pluginJar.toFile());
        } catch (IOException e) {
            throw new PluginException(pluginJar + " JarFile create error " + e.getCause(), e);
        }
    }

    public static PluginJar fromFilePath(Path path) {
        return new PluginJar(path);
    }

    private static void verify(Path file) {
        if (!Files.exists(file)) {
            throw new PluginException(file + " File does not exist");
        }
        if (!Files.isRegularFile(file)) {
            throw new PluginException(file + " is not a file");
        }
        if (!Files.isReadable(file)) {
            throw new PluginException(file + " File cannot be read");
        }
    }

    public URL getURL() {
        return url;
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    public String getPluginId() {
        return manifest.getPluginId();
    }

    public String getPluginCompilerVersion() {
        return manifest.getPluginCompilerVersion();
    }

    public List<String> getPluginPackages() {
        return manifest.getPluginPackages();
    }

    public List<String> getPluginPackageRequirements() {
        return manifest.getPluginPackageRequirements();
    }

    @Override
    public String toString() {
        return "PluginJar{" +
                "url=" + url +
                ", jarFile=" + jarFile +
                ", manifest=" + manifest +
                '}';
    }
}
