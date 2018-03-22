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

package com.navercorp.pinpoint.common.plugin;

import com.navercorp.pinpoint.common.util.Assert;

import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarPlugin<T> implements Plugin<T> {
    private final URL pluginUrl;
    private final JarFile pluginJarFIle;

    private final List<T> instanceList;
    private final List<String> packageList;

    public JarPlugin(URL pluginUrl, JarFile pluginJarFIle, List<T> instanceList, List<String> packageList) {
        this.pluginUrl = Assert.requireNonNull(pluginUrl, "plugin must not be null");
        this.pluginJarFIle = Assert.requireNonNull(pluginJarFIle, "pluginJar must not be null");
        this.instanceList = Assert.requireNonNull(instanceList, "instanceList must not be null");
        this.packageList = Assert.requireNonNull(packageList, "packageList must not be null");
    }

    @Override
    public URL getURL() {
        return pluginUrl;
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
        return pluginJarFIle;
    }

    @Override
    public String toString() {
        return "Plugin{" +
                "pluginUrl=" + pluginUrl +
                ", pluginJarFIle=" + pluginJarFIle +
                ", instanceList=" + instanceList +
                ", packageList=" + packageList +
                '}';
    }
}
