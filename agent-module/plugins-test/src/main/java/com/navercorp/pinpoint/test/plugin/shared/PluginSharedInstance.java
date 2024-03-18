/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.classloader.PluginTestClassLoader;

public class PluginSharedInstance {

    private String className;
    private String sharedClassName;
    private PluginTestClassLoader classLoader;
    private Class<?> sharedClass;

    Object object = null;

    public PluginSharedInstance(String className, String sharedClassName, PluginTestClassLoader classLoader) {
        this.className = className;
        this.sharedClassName = sharedClassName;
        this.classLoader = classLoader;
    }

    public void before() {
        try {
            Class<?> testClass = classLoader.loadClass(className);
            this.sharedClass = classLoader.loadClass(sharedClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Thread thread = Thread.currentThread();
        final ClassLoader currentClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(this.classLoader);
            this.object = sharedClass.newInstance();
            if (object instanceof SharedTestLifeCycle) {
                ((SharedTestLifeCycle) object).beforeAll();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            thread.setContextClassLoader(currentClassLoader);
        }
    }

    public void after() {
        Thread thread = Thread.currentThread();
        final ClassLoader currentClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(this.classLoader);
            if (object instanceof SharedTestLifeCycle) {
                ((SharedTestLifeCycle) object).afterAll();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            thread.setContextClassLoader(currentClassLoader);
        }
    }

    public void clear() {
        if (classLoader != null) {
            classLoader.clear();
            classLoader = null;
        }

    }

    @Override
    public String toString() {
        return "PluginSharedInstance{" +
                "className='" + className + '\'' +
                ", sharedClassName='" + sharedClassName + '\'' +
                ", classLoader=" + classLoader +
                '}';
    }
}
