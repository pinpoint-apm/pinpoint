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

package com.navercorp.pinpoint.bootstrap.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * PinpointClassLoader loads a class in the profiler lib directory and delegates to load the other classes to parent classloader
 * Dead lock could happen in case of standalone java application.
 * Don't delegate to parents classlaoder if classes are in the profiler lib directory
 *
 * @author emeroad
 */
public class Java9ClassLoader extends BaseClassLoader {

    static {
        if (ClassLoader.registerAsParallelCapable()) {
            throw new IllegalStateException("registerAsParallelCapable() fail");
        }
    }

    private final BootLoader bootLoader = new Java9BootLoader();


    public Java9ClassLoader(URL[] urls, ClassLoader parent, LibClass libClass) {
        super(urls, parent, libClass);
    }

    public Java9ClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent, PROFILER_LIB_CLASS);
    }

    @Override
    protected Object getClassLoadingLock0(String name) {
        return getClassLoadingLock(name);
    }

    @Override
    protected URL findBootstrapResource0(String name) {
        return bootLoader.findResource(name);
    }

    protected Enumeration<URL> findBootstrapResources0(String name) throws IOException {
        return bootLoader.findResources(name);
    }

    @Override
    protected Class<?> findBootstrapClassOrNull0(ClassLoader classLoader, String name) {
        return bootLoader.findBootstrapClassOrNull(classLoader, name);
    }
}
