/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import org.junit.Test;

import java.util.Comparator;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class HierarchyCachesTest {

    @Test
    public void get() throws Exception {
        HierarchyCaches caches = new HierarchyCaches(8, 8);
        assertFalse(caches.get("java/lang/Runnable", "java/lang/Thread"));
        caches.put("java/lang/Runnable", "java/lang/Thread");
        assertTrue(caches.get("java/lang/Runnable", "java/lang/Thread"));

        caches.put("java/lang/Runnable", "java/util/concurrent/FutureTask");
        caches.put("java/lang/Runnable", "java/util/TimerTask");

        assertTrue(caches.get("java/lang/Runnable", "java/util/TimerTask"));


        // sorted
        TreeMap<String, String> packageNameBasedIndex = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String key1, String key2) {
                return key1.length() - key2.length();
            }
        });

        packageNameBasedIndex.put("a", "a");
        packageNameBasedIndex.put("aa", "a");
        packageNameBasedIndex.put("bb", "a");
        packageNameBasedIndex.put("bbb", "a");
        packageNameBasedIndex.put("bbbb", "a");
        packageNameBasedIndex.put("c", "a");
        packageNameBasedIndex.put("cccc", "a");
        packageNameBasedIndex.put("ccccc", "a");
        packageNameBasedIndex.put("dddddddddddd", "a");
    }
}