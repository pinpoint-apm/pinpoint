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
import javassist.LoaderClassPath;

import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class SingleClassPool implements MultipleClassPool {

    private static final Object EXIST = new Object();

    private final NamedClassPool childClassPool;

    private final ConcurrentMap<ClassLoader, Object> checker = createWeakConcurrentMap();


    public SingleClassPool() {
        this.childClassPool = new NamedClassPool("singlePool");
        this.childClassPool.appendSystemPath();
    }

    private ConcurrentMap<ClassLoader, Object>createWeakConcurrentMap() {
        MapMaker mapMaker = new MapMaker();
        mapMaker.weakKeys();
        return mapMaker.makeMap();
    }

    @Override
    public NamedClassPool getClassPool(ClassLoader classLoader) {
        final Object hit = this.checker.get(classLoader);
        if (hit != null) {
            return childClassPool;
        }

        final ClassPath classPath = new LoaderClassPath(classLoader);
        synchronized (childClassPool) {
            Object exist = checker.putIfAbsent(classLoader, EXIST);
            if (exist != null) {
                return childClassPool;
            }
            this.childClassPool.appendClassPath(classPath);
            return childClassPool;
        }

    }




}
