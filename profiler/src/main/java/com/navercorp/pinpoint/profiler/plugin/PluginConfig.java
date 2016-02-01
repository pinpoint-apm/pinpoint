/*
 * Copyright 2016 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PINPOINT_PLUGIN_PACKAGE = "Pinpoint-Plugin-Package";
    public static final List<String> DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME = Collections.singletonList("com.navercorp.pinpoint.plugin");

    private final URL pluginJar;
    private final JarFile pluginJarFile;
    private String pluginJarURLExternalForm;

    private final ProfilerPlugin plugin;

    private final Instrumentation instrumentation;
    private final InstrumentClassPool classPool;
    private final String bootstrapCoreJarPath;

    private final ClassNameFilter pluginPackageFilter;

    public PluginConfig(URL pluginJar, ProfilerPlugin plugin, Instrumentation instrumentation, InstrumentClassPool classPool, String bootstrapCoreJarPath, ClassNameFilter pluginPackageFilter) {
        if (pluginJar == null) {
            throw new NullPointerException("pluginJar must not be null");
        }
        if (plugin == null) {
            throw new NullPointerException("plugin must not be null");
        }
        this.pluginJar = pluginJar;
        this.pluginJarFile = createJarFile(pluginJar);
        this.plugin = plugin;

        this.instrumentation = instrumentation;
        this.classPool = classPool;
        this.bootstrapCoreJarPath = bootstrapCoreJarPath;

        this.pluginPackageFilter = pluginPackageFilter;
    }




    public ProfilerPlugin getPlugin() {
        return plugin;
    }

    public URL getPluginJar() {
        return pluginJar;
    }

    public JarFile getPluginJarFile() {
        return pluginJarFile;
    }

    public String getPluginJarURLExternalForm() {
        if (this.pluginJarURLExternalForm == null) {
            this.pluginJarURLExternalForm = pluginJar.toExternalForm();
        }
        return this.pluginJarURLExternalForm;
    }

    private JarFile createJarFile(URL pluginJar) {
        try {
            final URI uri = pluginJar.toURI();
            return new JarFile(new File(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException("URISyntax error. " + e.getCause(), e);
        } catch (IOException e) {
            throw new RuntimeException("IO error. " + e.getCause(), e);
        }
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public InstrumentClassPool getClassPool() {
        return classPool;
    }

    public String getBootstrapCoreJarPath() {
        return bootstrapCoreJarPath;
    }

    public ClassNameFilter getPluginPackageFilter() {
        return pluginPackageFilter;
    }


}