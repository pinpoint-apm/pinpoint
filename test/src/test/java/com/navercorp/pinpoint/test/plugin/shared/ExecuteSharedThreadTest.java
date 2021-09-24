/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class ExecuteSharedThreadTest {

    @Test
    public void threadTest() {
        Assert.assertEquals(0, SimpleTest.RUN_BEFORE_SHARED_CLAZZ_COUNT);
        Assert.assertEquals(0, SimpleTest.RUN_AFTER_SHARED_CLAZZ_COUNT);

        ExecuteSharedThread executeSharedThread = new ExecuteSharedThread(SimpleTest.class.getName(), SimpleTest.class.getClassLoader());
        executeSharedThread.startBefore();
        executeSharedThread.awaitBeforeCompleted(1, TimeUnit.SECONDS);
        Assert.assertEquals(1, SimpleTest.RUN_BEFORE_SHARED_CLAZZ_COUNT);
        Assert.assertEquals(0, SimpleTest.RUN_AFTER_SHARED_CLAZZ_COUNT);

        executeSharedThread.startAfter();
        executeSharedThread.join(1000);
        Assert.assertEquals(1, SimpleTest.RUN_BEFORE_SHARED_CLAZZ_COUNT);
        Assert.assertEquals(1, SimpleTest.RUN_AFTER_SHARED_CLAZZ_COUNT);
    }

    @Test(expected = IllegalStateException.class)
    public void illegalStateTest1() {
        ExecuteSharedThread executeSharedThread = new ExecuteSharedThread(SimpleTest.class.getName(), SimpleTest.class.getClassLoader());
        executeSharedThread.awaitBeforeCompleted(1, TimeUnit.SECONDS);
    }

    @Test(expected = IllegalStateException.class)
    public void illegalStateTest2() {
        ExecuteSharedThread executeSharedThread = new ExecuteSharedThread(SimpleTest.class.getName(), SimpleTest.class.getClassLoader());
        executeSharedThread.startAfter();
    }

    static class SimpleTest {

        private static int RUN_BEFORE_SHARED_CLAZZ_COUNT;
        private static int RUN_AFTER_SHARED_CLAZZ_COUNT;


        @BeforeSharedClass
        public static void sharedSetup() {
            RUN_BEFORE_SHARED_CLAZZ_COUNT++;
        }

        @AfterSharedClass
        public static void sharedTearDown() {
            RUN_AFTER_SHARED_CLAZZ_COUNT++;
        }

    }

}
