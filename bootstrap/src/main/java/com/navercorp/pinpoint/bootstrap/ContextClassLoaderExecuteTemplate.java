/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import java.util.concurrent.Callable;

/**
 * This template is used for changing the current thread's classloader to the assigned one and executing a callable.
 *
 * @author emeroad
 */
public class ContextClassLoaderExecuteTemplate<V> {
    private final ClassLoader classLoader;

    public ContextClassLoaderExecuteTemplate(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }
        this.classLoader = classLoader;
    }

    public V execute(Callable<V> callable) throws BootStrapException {
        try {
            final Thread currentThread = Thread.currentThread();
            final ClassLoader before = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(ContextClassLoaderExecuteTemplate.this.classLoader);
            try {
                return callable.call();
            } finally {
                // even though  the "BEFORE" classloader  is null, rollback  is needed.
                // if an exception occurs BEFORE callable.call(), the call flow can't reach here.
                // so  rollback  here is right.
                currentThread.setContextClassLoader(before);
            }
        } catch (BootStrapException ex){
            throw ex;
        } catch (Exception ex) {
            throw new BootStrapException("execute fail. Error:" + ex.getMessage(), ex);
        }
    }
}
