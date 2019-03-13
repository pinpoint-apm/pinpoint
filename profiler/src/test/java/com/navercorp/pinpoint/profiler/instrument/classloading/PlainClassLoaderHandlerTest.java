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

import com.navercorp.pinpoint.profiler.plugin.JarPlugin;
import com.navercorp.pinpoint.profiler.plugin.ClassNameFilter;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.jar.JarFile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PlainClassLoaderHandlerTest {

    @Test
    public void injectClass_bootstrapClass() {
        PluginConfig pluginConfig = newPluginConfig();

        ClassInjector plainClassLoaderHandler = new PlainClassLoaderHandler(pluginConfig);
        try {
            plainClassLoaderHandler.injectClass(this.getClass().getClassLoader(), "com.navercorp.pinpoint.bootstrap.Test");
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.fail();
        } catch (Exception e) {

        }
    }

//    @Test
//    public void injectClass() {
//        PluginConfig pluginConfig = newPluginConfig();
//
//        PlainClassLoaderHandler plainClassLoaderHandler = new PlainClassLoaderHandler(pluginConfig);
//        plainClassLoaderHandler.injectClass(this.getClass().getClassLoader(), "java.lang.String");
//
//    }

    private PluginConfig newPluginConfig() {
        JarPlugin plugin = mock(JarPlugin.class);
        JarFile jarFile = mock(JarFile.class);
        when(plugin.getJarFile()).thenReturn(jarFile);

        ClassNameFilter filter = new ClassNameFilter() {
            @Override
            public boolean accept(String className) {
                return ClassNameFilter.ACCEPT;
            }
        };
        PluginConfig pluginConfig = new PluginConfig(plugin, filter);
        return pluginConfig;
    }
}