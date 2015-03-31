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

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.Maps;
import javassist.ClassPath;
import javassist.LoaderClassPath;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class IsolateMultipleClassPool implements MultipleClassPool {

    private static final AtomicInteger ID = new AtomicInteger();

    private static final ClassLoader AGENT_CLASS_LOADER = IsolateMultipleClassPool.class.getClassLoader();

    private final NamedClassPool standardClassPool;

    private final ConcurrentMap<ClassLoader, NamedClassPool> classPoolMap;

    private final EventListener eventListener;

    public static final boolean DEFAULT_CHILD_FIRST_LOOKUP = true;
    private final boolean childFirstLookup;


    public static final EventListener EMPTY_EVENT_LISTENER = new EventListener() {
        @Override
        public void onCreateClassPool(ClassLoader classLoader, NamedClassPool classPool) {
        }
    };

    interface ClassPoolHandler {
        void handleClassPool(NamedClassPool classPool);
    }

    interface EventListener {
        void onCreateClassPool(ClassLoader classLoader, NamedClassPool classPool);
    }

    public IsolateMultipleClassPool(EventListener eventListener, ClassPoolHandler systemClassPoolHandler) {
        this(DEFAULT_CHILD_FIRST_LOOKUP, eventListener, systemClassPoolHandler);
    }

    public IsolateMultipleClassPool() {
        this(DEFAULT_CHILD_FIRST_LOOKUP, EMPTY_EVENT_LISTENER, null);
    }

    public IsolateMultipleClassPool(boolean childFirstLookup, EventListener eventListener, ClassPoolHandler systemClassPoolHandler) {
        if (eventListener == null) {
            throw new NullPointerException("eventListener must not be null");
        }

        this.standardClassPool = createSystemClassPool(systemClassPoolHandler);
        this.classPoolMap = Maps.newWeakConcurrentMap();
        this.eventListener = eventListener;
        this.childFirstLookup = childFirstLookup;
    }



    private NamedClassPool createSystemClassPool(ClassPoolHandler systemClassPoolHandler) {
        NamedClassPool systemClassPool = new NamedClassPool("standardClassPool");
        systemClassPool.appendSystemPath();
        if (systemClassPoolHandler != null ) {
            systemClassPoolHandler.handleClassPool(systemClassPool);

        }
        return systemClassPool;
    }

    @Override
    public NamedClassPool getClassPool(ClassLoader classLoader) {
        if (ClassLoaderUtils.isJvmClassLoader(classLoader)) {
            return standardClassPool;
        }

        if (AGENT_CLASS_LOADER == classLoader ){
            throw new IllegalArgumentException("unexpected classLoader access. classLoader:" + classLoader);
        }
        final NamedClassPool hit = this.classPoolMap.get(classLoader);
        if (hit != null) {
            return hit;
        }
        NamedClassPool newClassPool = createClassPool(classLoader);
        return put(classLoader, newClassPool);
    }

    private NamedClassPool put(ClassLoader classLoader, NamedClassPool classPool) {
        final NamedClassPool exist = this.classPoolMap.putIfAbsent(classLoader, classPool);
        if (exist != null) {
            return exist;
        }
        fireOnCreateClassPool(classLoader, classPool);
        return classPool;
    }

    private void fireOnCreateClassPool(ClassLoader classLoader, NamedClassPool classPool) {
        eventListener.onCreateClassPool(classLoader, classPool);
    }


    private NamedClassPool createClassPool(ClassLoader classLoader) {
        String classLoaderName = classLoader.toString();
        NamedClassPool newClassPool = new NamedClassPool(standardClassPool, classLoaderName + "-" + getNextId());
        if (childFirstLookup) {
            newClassPool.childFirstLookup = true;
        }

        final ClassPath classPath = new LoaderClassPath(classLoader);
        newClassPool.appendClassPath(classPath);

        return newClassPool;
    }

    private int getNextId() {
        return ID.getAndIncrement();
    }


    public int size() {
        return this.classPoolMap.size();
    }

    // for Test
    Collection<NamedClassPool> values() {
        return classPoolMap.values();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IsolateMultipleClassPool{");
        sb.append("classPoolMap=").append(classPoolMap);
        sb.append('}');
        return sb.toString();
    }


}
