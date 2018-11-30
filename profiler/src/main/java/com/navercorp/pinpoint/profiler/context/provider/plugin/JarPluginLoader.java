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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.navercorp.pinpoint.common.plugin.JarFileUtils;
import com.navercorp.pinpoint.common.plugin.JarPlugin;
import com.navercorp.pinpoint.common.plugin.Plugin;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarFile;

/**
 * TODO Loading all plugins with a single class loader could cause class collisions.
 * Also, with current implementation, plugins can use dependencies by putting them in the plugin directory too.
 * But it can lead to dependency collision between plugins because they are loaded by a single class loader.
 * <p>
 * How can we prevent this?
 * A ClassLoader per plugin could do it but then we have to create "N of target class loader" x "N of plugin" class loaders.
 * It seems too much. For now, Just leave it as it is.
 *
 * @author Jongho Moon <jongho.moon@navercorp.com>
 * @author emeroad
 */
public class JarPluginLoader implements PluginLoader {
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClassLoader parentClassLoader;

    private final List<Entry> serviceLoaderList;

    public JarPluginLoader(List<String> pluginJar, ClassLoader parentClassLoader) {
        this.serviceLoaderList = loadPluginJar(pluginJar);
        //@Nullable
        this.parentClassLoader = parentClassLoader;
    }

    private List<Entry> loadPluginJar(List<String> pluginJar) {
        Assert.requireNonNull(pluginJar, "pluginJar must not be null");
        // considering cl shared policy
        return isolationPolicy(pluginJar);
    }

    private List<Entry> isolationPolicy(List<String> pluginJar) {
        final List<Entry> list = new ArrayList<Entry>();
        for (String filePath : pluginJar) {
            final File file = toFile(filePath);
            final URL url = toUrl(file);

            final ClassLoader pluginClassLoader = createPluginClassLoader(new URL[]{url}, parentClassLoader);
            Entry entry = new Entry(url, file, pluginClassLoader);
            list.add(entry);
        }
        return list;
    }

    private URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL:" + file);
        }
    }

    private File toFile(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException(file + " File not exist");
        }
        if (!file.isFile()) {
            throw new RuntimeException(file + " is not file");
        }
        if (!file.canRead()) {
            throw new RuntimeException(file + " File cannot be read");
        }
        return file;
    }

    public static class Entry {
        private final URL filePath;
        private final File file;
        private final ClassLoader classLoader;

        public Entry(URL filePath, File file, ClassLoader classLoader) {
            this.filePath = Assert.requireNonNull(filePath, "filePath must not be null");
            this.file = Assert.requireNonNull(file, "file must not be null");
            this.classLoader = Assert.requireNonNull(classLoader, "classLoader must not be null");
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public URL getURL() {
            return filePath;
        }

        public File getFile() {
            return file;
        }
    }

    private ClassLoader createPluginClassLoader(final URL[] urls, final ClassLoader parent) {
        if (logger.isDebugEnabled()) {
            logger.debug("createPluginClassLoader(urls = [{}], parent = [{}])", Arrays.toString(urls), parent);
        }
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return new URLClassLoader(urls, parent);
                }
            });
        } else {
            return new URLClassLoader(urls, parent);
        }
    }


    private static <T> List<T> toList(Iterable<T> iterable, Class<T> serviceType) {
        final List<T> list = new ArrayList<T>();
        for (T plugin : iterable) {
            list.add(serviceType.cast(plugin));
        }
        return  list;
    }

    @Override
    public <T> List<Plugin<T>> load(Class<T> serviceType) {
        List<Plugin<T>> result = new ArrayList<Plugin<T>>();
        for (Entry entry : serviceLoaderList) {
            final Plugin<T> plugin = newPlugin(serviceType, entry);
            result.add(plugin);
        }

        return result;
    }

    private <T> Plugin<T> newPlugin(Class<T> serviceType, Entry entry) {
        URL pluginURL = entry.getURL();
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType, entry.getClassLoader());
        List<T> pluginList = toList(serviceLoader, serviceType);
        JarFile jarFile = createJarFile(entry.getFile());
        String pluginPackages = JarFileUtils.getManifestValue(jarFile, PluginConfig.PINPOINT_PLUGIN_PACKAGE, PluginConfig.DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME);
        List<String> pluginPackageList = StringUtils.tokenizeToStringList(pluginPackages, ",");

        return new JarPlugin<T>(pluginURL, jarFile, pluginList, pluginPackageList);
    }


    private JarFile createJarFile(File pluginJar) {
        try {
            return new JarFile(pluginJar);
        } catch (IOException e) {
            throw new RuntimeException("IO error. " + e.getCause(), e);
        }
    }
}
