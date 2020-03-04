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

import com.navercorp.pinpoint.common.util.Assert;

import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarPlugin<T> implements Plugin<T> {

    private final PluginJar pluginJar;

    private final List<T> instanceList;
    private final List<String> packageList;

    public JarPlugin(PluginJar pluginJar, List<T> instanceList, List<String> packageList) {
        this.pluginJar = Assert.requireNonNull(pluginJar, "pluginJar");
        this.instanceList = Assert.requireNonNull(instanceList, "instanceList");
        this.packageList = Assert.requireNonNull(packageList, "packageList");
    }

    @Override
    public URL getURL() {
        return pluginJar.getUrl();
    }

    @Override
    public List<T> getInstanceList() {
        return instanceList;
    }

    @Override
    public List<String> getPackageList() {
        return packageList;
    }


    public JarFile getJarFile() {
        return pluginJar.getJarFile();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JarPlugin{");
        sb.append("pluginJar=").append(pluginJar);
        sb.append(", instanceList=").append(instanceList);
        sb.append(", packageList=").append(packageList);
        sb.append('}');
        return sb.toString();
    }
}
