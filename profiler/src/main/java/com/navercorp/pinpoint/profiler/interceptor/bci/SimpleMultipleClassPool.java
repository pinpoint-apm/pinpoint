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
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class SimpleMultipleClassPool implements MultipleClassPool {
    private static final AtomicInteger ID = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentMap<ClassLoader, NamedClassPool> classMap;
    private final NamedClassPool parentClassPool;


    public SimpleMultipleClassPool(NamedClassPool parentClassPool) {
        if (parentClassPool == null) {
            throw new NullPointerException("parentClassPool must not be null");
        }
        MapMaker mapMaker = new MapMaker();
        mapMaker.weakKeys();
        this.classMap = mapMaker.makeMap();
        this.parentClassPool = parentClassPool;
    }

    @Override
    public NamedClassPool getClassPool(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }
        final NamedClassPool hit = this.classMap.get(classLoader);
        if (hit != null) {
            return hit;
        }
        NamedClassPool newClassPool = createClassPool(classLoader, parentClassPool);
        return put(classLoader, newClassPool);
    }

    private NamedClassPool put(ClassLoader classLoader, NamedClassPool classPool) {
        final NamedClassPool exist = this.classMap.putIfAbsent(classLoader, classPool);
        if (exist != null) {
            return exist;
        }
        return classPool;
    }




    private NamedClassPool createClassPool(ClassLoader classLoader, NamedClassPool parentClassPool) {
        String classLoaderName = classLoader.getClass().getName();
        NamedClassPool newClassPool = new NamedClassPool(parentClassPool, classLoaderName + "-" + ID.getAndIncrement());
        newClassPool.childFirstLookup = true;

        final ClassPath classPath = new LoaderClassPath(classLoader);
        newClassPool.appendClassPath(classPath);
        return newClassPool;
    }



    public int size() {
        return this.classMap.size();
    }


    public Collection<NamedClassPool> values() {
        return classMap.values();
    }
}
