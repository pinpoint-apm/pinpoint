/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Jongho Moon
 *
 */
public class ClassLoaderLock {
    private static final ConcurrentMap<ClassLoader, LockInfo> map = new ConcurrentHashMap<ClassLoader, LockInfo>();

    public static void register(ClassLoader parent, ClassLoader child) {
        LockInfo info = new LockInfo(parent, child, new ReentrantLock());
        map.put(parent, info);
        map.put(child, info);
    }
    
    public static void lock(ClassLoader loader) {
        LockInfo info = map.get(loader);
        
        
        if (info == null) {
            return;
        }
        
        while (!info.lock.tryLock()) {
            try {
                if (Thread.holdsLock(info.child)) {
                    info.child.wait(0, 1);
                } else if (Thread.holdsLock(info.parent)) {
                    info.parent.wait(0, 1);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted", e);
            }
        }
    }
    
    public static void unlock(ClassLoader loader) {
        LockInfo info = map.get(loader);
        
        if (info == null) {
            return;
        }
        
        info.lock.unlock();
    }

    private static class LockInfo {
        private final ClassLoader parent;
        private final ClassLoader child;
        private final ReentrantLock lock;
        
        public LockInfo(ClassLoader parent, ClassLoader child, ReentrantLock lock) {
            this.parent = parent;
            this.child = child;
            this.lock = lock;
        }
    }
}
