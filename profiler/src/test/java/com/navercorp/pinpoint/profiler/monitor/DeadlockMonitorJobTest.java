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

package com.navercorp.pinpoint.profiler.monitor;

import org.junit.Ignore;

import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DeadlockMonitorJobTest {

    private static final long DEADLOCK_TIME = 5000;


    private Object lock1 = new Object();
    private Object lock2 = new Object();

    @Ignore
//    @Test
    public void testName() throws Exception {
        Thread thread1 = null;
        Thread thread2 = null;
        try {
            thread1 = new Thread1("DEADLOCK THREAD-1", DEADLOCK_TIME, lock1, lock2);
            thread1.start();

            thread2 = new Thread1("DEADLOCK THREAD-2", DEADLOCK_TIME, lock2, lock1);
            thread2.start();

            Thread.sleep(DEADLOCK_TIME + 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DeadlockThreadRegistry registry = new DeadlockThreadRegistry();

        DeadlockMonitorThread deadlockMonitorThread = new DeadlockMonitorThread(registry, 3000);
        deadlockMonitorThread.start();

        System.out.println(registry);

        while (true) {
            Set<Long> deadlockedThreadIdSet = registry.getDeadlockedThreadIdSet();
            if (deadlockedThreadIdSet != null && deadlockedThreadIdSet.size() > 0) {
                System.out.println(deadlockedThreadIdSet);
                break;
            }
        }

        deadlockMonitorThread.stop();

        Thread.sleep(5000);
    }


    class Thread1 extends Thread {

        private final String name;
        private final long waitTime;
        private final Object lock1;
        private final Object lock2;


        public Thread1(String name, long waitTime, Object lock1, Object lock2) {
            super();
            this.name = name;
            this.waitTime = waitTime;
            this.lock1 = lock1;
            this.lock2 = lock2;
            setName(name);
        }


        @Override
        public void run() {

            synchronized (lock1) {
                long startTime = System.currentTimeMillis();
                while (true) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime > waitTime) {
                        break;
                    }
                }

                synchronized (lock2) {
                }
            }

        }
    }

}
