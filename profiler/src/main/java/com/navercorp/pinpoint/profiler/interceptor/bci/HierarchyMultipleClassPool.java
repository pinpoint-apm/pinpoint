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

    private static final ClassLoader SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();
    private static final ClassLoader EXT_CLASS_LOADER = SYSTEM_CLASS_LOADER.getParent();

    private final ConcurrentMap<ClassLoader, ClassPool> classMap;
    private final ClassPool parentClassPool;


    public HierarchyMultipleClassPool(ClassPool parentClassPool) {
        if (parentClassPool == null) {
            throw new NullPointerException("parentClassPool must not be null");
        }
        MapMaker mapMaker = new MapMaker();
        mapMaker.weakKeys();
        this.classMap = mapMaker.makeMap();
        this.parentClassPool = parentClassPool;
    }

    @Override
    public ClassPool getClassPool(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }
        final ClassPool hit = this.classMap.get(classLoader);
        if (hit != null) {
            return hit;
        }
        // concurrent classPool create
        prepareHierarchyClassPool(classLoader);

        final ClassPool classPool = this.classMap.get(classLoader);
        if (classPool == null) {
            logger.warn("ClassPool FindError :{}" + classLoader);
            return null;
        }
        return classPool;
    }

    private ClassPool put(ClassLoader classLoader, ClassPool classPool) {
        final ClassPool exist = this.classMap.putIfAbsent(classLoader, classPool);
        if (exist != null) {
            return exist;
        }
        return classPool;
    }




    private ClassPool createClassPool(ClassLoader classLoader, ClassPool parentClassPool) {
        String classLoaderName = classLoader.getClass().getName();
        ClassPool newClassPool = new NamedClassPool(parentClassPool, classLoaderName + "-" + ID.getAndIncrement());
        newClassPool.childFirstLookup = true;

        final ClassPath classPath = new LoaderClassPath(classLoader);
        newClassPool.appendClassPath(classPath);
        return newClassPool;
    }

    private void prepareHierarchyClassPool(ClassLoader findClassLoader) {

        final Collection<ClassLoader> classLoaderHierarchyList = findClassLoaderHierarchy(findClassLoader);
        logger.debug("ClassLoaderHierarchy:{}", classLoaderHierarchyList);

        ClassPool parentClassPool = this.parentClassPool;
        for (ClassLoader classLoader : classLoaderHierarchyList) {
            final ClassPool existClassPool = this.classMap.get(classLoader);
            if (existClassPool != null) {
                parentClassPool = existClassPool;
            } else {
                ClassPool classPool = createClassPool(classLoader, parentClassPool);
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
            if (isRoot(parent)) {
                break;
            }
            classLoaderHierarchyList.addFirst(parent);
            classLoader = parent;
        }
        return classLoaderHierarchyList;
    }

    private boolean isRoot(ClassLoader parent) {
        return parent == SYSTEM_CLASS_LOADER || parent == EXT_CLASS_LOADER || parent == null;
    }


    public int size() {
        return this.classMap.size();
    }


    public Collection<ClassPool> values() {
        return classMap.values();
    }
}
