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

package com.navercorp.pinpoint.profiler.instrument.classpool;

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.Maps;
import javassist.ClassPath;
import javassist.LoaderClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class HierarchyMultipleClassPool implements MultipleClassPool {
    private static final AtomicInteger ID = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ClassLoader SYSTEM = ClassLoader.getSystemClassLoader();

    private final ConcurrentMap<ClassLoader, NamedClassPool> classMap;
    private final NamedClassPool parentClassPool;


    public HierarchyMultipleClassPool(NamedClassPool parentClassPool) {
        if (parentClassPool == null) {
            throw new NullPointerException("parentClassPool must not be null");
        }
        this.classMap = Maps.newWeakConcurrentMap();
        this.parentClassPool = parentClassPool;
    }


    public HierarchyMultipleClassPool() {
        this.classMap = Maps.newWeakConcurrentMap();
        this.parentClassPool = new NamedClassPool("system");
        parentClassPool.appendSystemPath();
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
        // concurrent classPool create
        prepareHierarchyClassPool(classLoader);

        final NamedClassPool classPool = this.classMap.get(classLoader);
        if (classPool == null) {
            throw new IllegalStateException("unexpected condition. ClassPool not found. classLoader:" + classLoader);
        }
        return classPool;
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

    private void prepareHierarchyClassPool(ClassLoader findClassLoader) {

        final Collection<ClassLoader> classLoaderHierarchyList = findClassLoaderHierarchy(findClassLoader);
        logger.debug("ClassLoaderHierarchy:{}", classLoaderHierarchyList);

        NamedClassPool parentClassPool = this.parentClassPool;
        for (ClassLoader classLoader : classLoaderHierarchyList) {
            final NamedClassPool existClassPool = this.classMap.get(classLoader);
            if (existClassPool != null) {
                parentClassPool = existClassPool;
            } else {
                NamedClassPool classPool = createClassPool(classLoader, parentClassPool);
                parentClassPool = put(classLoader, classPool);
            }
        }
    }

    private Collection<ClassLoader> findClassLoaderHierarchy(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }

        Deque<ClassLoader> classLoaderHierarchyList = new LinkedList<ClassLoader>();
        classLoaderHierarchyList.addFirst(classLoader);

        // search classLoader;
        ClassLoader parent;
        while (true) {
            parent = classLoader.getParent();
            if (ClassLoaderUtils.isJvmClassLoader(parent)) {
                classLoaderHierarchyList.addFirst(SYSTEM);
                break;
            }
            classLoaderHierarchyList.addFirst(parent);
            classLoader = parent;
        }
        return classLoaderHierarchyList;
    }


    public int size() {
        return this.classMap.size();
    }


    public Collection<NamedClassPool> values() {
        return classMap.values();
    }
}
