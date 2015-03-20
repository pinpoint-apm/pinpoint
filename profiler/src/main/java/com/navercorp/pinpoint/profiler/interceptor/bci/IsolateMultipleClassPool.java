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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import com.google.common.collect.MapMaker;
import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.exception.PinpointException;
import javassist.ClassPath;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class IsolateMultipleClassPool implements MultipleClassPool {

    private static final AtomicInteger ID = new AtomicInteger();

    private final NamedClassPool standardClassPool;

    private final ConcurrentMap<ClassLoader, NamedClassPool> classMap;

    private final EventListener eventListener;


    public IsolateMultipleClassPool() {
        this.standardClassPool = createSystemClassPool(null);
        this.classMap = createWeakConcurrentMap();
        this.eventListener = null;
    }

    public IsolateMultipleClassPool(EventListener eventListener, String bootStrapJarPath) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener must not be null");
        }

        this.standardClassPool = createSystemClassPool(bootStrapJarPath);
        this.classMap = createWeakConcurrentMap();
        this.eventListener = eventListener;
    }


    private ConcurrentMap<ClassLoader, NamedClassPool> createWeakConcurrentMap() {
        MapMaker mapMaker = new MapMaker();
        mapMaker.weakKeys();
        return mapMaker.makeMap();
    }

    private NamedClassPool createSystemClassPool(String bootStrapJarPath) {
        NamedClassPool systemClassPool = new NamedClassPool("standardClassPool");
        systemClassPool.appendSystemPath();
        if (bootStrapJarPath != null ) {
            try {
                systemClassPool.appendClassPath(bootStrapJarPath);
            } catch (NotFoundException ex) {
                throw new PinpointException("bootStrapJar not found. Caused by:" + ex.getMessage(), ex);
            }
        }
        return systemClassPool;
    }

    @Override
    public NamedClassPool getClassPool(ClassLoader classLoader) {
        if (ClassLoaderUtils.isStandardClassLoader(classLoader)) {
            return standardClassPool;
        }
        final NamedClassPool hit = this.classMap.get(classLoader);
        if (hit != null) {
            return hit;
        }
        NamedClassPool newClassPool = createClassPool(classLoader);
        return put(classLoader, newClassPool);
    }

    private NamedClassPool put(ClassLoader classLoader, NamedClassPool classPool) {
        final NamedClassPool exist = this.classMap.putIfAbsent(classLoader, classPool);
        if (exist != null) {
            return exist;
        }
        fireOnCreateClassPool(classLoader, classPool);
        return classPool;
    }

    private void fireOnCreateClassPool(ClassLoader classLoader, NamedClassPool classPool) {
        final EventListener eventListener = this.eventListener;
        if (eventListener != null) {
            eventListener.onCreateClassPool(classLoader, classPool);
        }
    }


    private NamedClassPool createClassPool(ClassLoader classLoader) {
        String classLoaderName = classLoader.toString();
        NamedClassPool newClassPool = new NamedClassPool(standardClassPool, classLoaderName + "-" + getNextId());

        final ClassPath classPath = new LoaderClassPath(classLoader);
        newClassPool.appendClassPath(classPath);

//        newClassPool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));

        newClassPool.appendSystemPath();

        return newClassPool;
    }

    private int getNextId() {
        return ID.getAndIncrement();
    }


    public int size() {
        return this.classMap.size();
    }


    public Collection<NamedClassPool> values() {
        return classMap.values();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IsolateMultipleClassPool{");
        sb.append("classMap=").append(classMap);
        sb.append('}');
        return sb.toString();
    }

}
