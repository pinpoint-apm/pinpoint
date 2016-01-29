/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.common.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author Taejin Koo
 */
public class OrderedThreadPoolTaskHolder<K> {

    private static final int DEFAULT_LOCK_SIZE = 16;

    private final ReadWriteLock mainLock = new ReentrantReadWriteLock();

    private final int lockSize;
    private final Object[] locks;

    private final ConcurrentHashMap<K, List<Runnable>> runnableMap = new ConcurrentHashMap<K, List<Runnable>>();

    public OrderedThreadPoolTaskHolder() {
        this(DEFAULT_LOCK_SIZE);
    }

    public OrderedThreadPoolTaskHolder(int lockSize) {
        this.lockSize = lockSize;

        this.locks = new Object[lockSize];
        for (int i = 0; i < lockSize; i++) {
            this.locks[i] = new Object();
        }
    }

    boolean putTaskAndReturnKeyIsFirst(K key, Runnable task) {
        assertNotNull(key);
        assertNotNull(task);

        boolean isFirst = false;


        int hash = getHash(key);
        mainLock.readLock().lock();
        try {
            synchronized (locks[hash]) {
                List<Runnable> runnableList = runnableMap.get(key);
                if (runnableList == null) {
                    runnableList = new ArrayList<Runnable>();
                    runnableMap.put(key, runnableList);
                    isFirst = true;
                }

                runnableList.add(task);
                return isFirst;
            }
        } finally {
            mainLock.readLock().unlock();
        }
    }

    Runnable getTask(K key) {
        assertNotNull(key);

        int hash = getHash(key);
        mainLock.readLock().lock();
        try {
            synchronized (locks[hash]) {
                List<Runnable> runnableList = runnableMap.get(key);
                if (runnableList == null || runnableList.isEmpty()) {
                    return null;
                }

                return runnableList.remove(0);
            }
        } finally {
            mainLock.readLock().unlock();
        }
    }

    boolean removeIfEmpty(K key) {
        assertNotNull(key);

        int hash = getHash(key);
        mainLock.readLock().lock();
        try {
            synchronized (locks[hash]) {
                List<Runnable> runnableList = runnableMap.get(key);
                if (runnableList == null || runnableList.isEmpty()) {
                    runnableMap.remove(key);
                    return true;
                }
                return false;
            }
        } finally {
            mainLock.readLock().unlock();
        }
    }

    List<Runnable> clear() {
        mainLock.writeLock().lock();
        try {
            List<Runnable> notCompletedRunnableList = new ArrayList<Runnable>();
            for (Map.Entry<K, List<Runnable>> entry : runnableMap.entrySet()) {
                notCompletedRunnableList.addAll(entry.getValue());
            }
            runnableMap.clear();
            return notCompletedRunnableList;
        } finally {
            mainLock.writeLock().unlock();
        }

    }

    private int getHash(K key) {
        int hashCode = Math.abs(key.hashCode()) % lockSize;
        return hashCode;
    }

    private void assertNotNull(Object o) {
        if (o == null) {
            throw new NullPointerException("null");
        }
    }

}
