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

package com.navercorp.pinpoint.common.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * @author HyunGil Jeong
 */
public class PluginJar {

    public static final String PINPOINT_PLUGIN_ID = "Pinpoint-Plugin-Id";
    public static final String PINPOINT_PLUGIN_PACKAGE = "Pinpoint-Plugin-Package";
    public static final String DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME = "com.navercorp.pinpoint.plugin";

    private final URL url;
    private final JarFile jarFile;

    private final String pluginId;
    private final String pluginPackages;

    private PluginJar(URL url, JarFile jarFile) {
        this.url = url;
        this.jarFile = jarFile;
        this.pluginId = JarFileUtils.getManifestValue(jarFile, PINPOINT_PLUGIN_ID, null);
        this.pluginPackages = JarFileUtils.getManifestValue(jarFile, PINPOINT_PLUGIN_PACKAGE, DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME);
    }

    public static PluginJar fromFilePath(String filePath) {
        final File file = toFile(filePath);
        return fromFile(file);
    }

    public static PluginJar fromFile(File file) {
        final URL url = toUrl(file);
        final JarFile jarFile = createJarFile(file);
        return new PluginJar(url, jarFile);
    }

    private static File toFile(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException(file + " File does not exist");
        }
        if (!file.isFile()) {
            throw new RuntimeException(file + " is not a file");
        }
        if (!file.canRead()) {
            throw new RuntimeException(file + " File cannot be read");
        }
        return file;
    }

    private static URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL:" + file);
        }
    }

    private static JarFile createJarFile(File pluginJar) {
        try {
            return new JarFile(pluginJar);
        } catch (IOException e) {
            throw new RuntimeException("IO error. " + e.getCause(), e);
        }
    }

    public URL getUrl() {
        return url;
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginPackages() {
        return pluginPackages;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginJar{");
        sb.append("url=").append(url);
        sb.append(", jarFile=").append(jarFile);
        sb.append(", pluginId='").append(pluginId).append('\'');
        sb.append(", pluginPackages='").append(pluginPackages).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
