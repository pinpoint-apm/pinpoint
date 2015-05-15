/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.plugin;

import java.net.URL;

import org.junit.Test;

/**
 * @author Jongho Moon
 *
 */
public class Java7PluginClassLoaderTest {

    @Test
    public void test() throws Exception {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        ClassLoader child = new Java7PluginClassLoader(new URL[0], parent);
        
        final int run = 10000;
        final int threadNum = 16;
        Thread[] thread = new Thread[threadNum];
        
        for (int i = 0; i < threadNum; i++) {
            if (i % 2 == 0) {
                thread[i] = new Thread(new Parent(parent, child, run));
            } else {
                thread[i] = new Thread(new Child(child, run));
            }
        }
        
        for (int i = 0; i < threadNum; i++) {
            thread[i].start();
        }
        
        System.out.println("Wait workers...");
        
        for (int i = 0; i < threadNum; i++) {
            thread[i].join();
        }
    }

    
    private static class Parent implements Runnable {
        private final ClassLoader parent;
        private final ClassLoader child;
        private final int count;

        public Parent(ClassLoader parent, ClassLoader child, int count) {
            this.parent = parent;
            this.child = child;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                System.out.println(this + ": " + i);
                synchronized(parent) {
                    try {
                        child.loadClass("no.such.Class");
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
    }
    
    private static class Child implements Runnable {
        private final ClassLoader child;
        private final int count;
        
        public Child(ClassLoader child, int count) {
            this.child = child;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                System.out.println(this + ": " + i);
                synchronized(child) {
                    try {
                        child.loadClass("no.such.Class");
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
    }
}
