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

package com.navercorp.pinpoint.rpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class FutureTest {

    @Test
    public void simpleTest1() {
        DefaultFuture<String> future = new DefaultFuture<>();

        SimpleListener<String> listener1 = new SimpleListener<>();

        future.setListener(listener1);
//        future.addListener(listener2);

        Assertions.assertFalse(listener1.isFinished());
//        Assertions.assertFalse(listener2.isFinished());

        future.setResult("Hello");

        Assertions.assertTrue(listener1.isFinished());
//        Assertions.assertTrue(listener2.isFinished());
    }

    @Test
    public void simpleTest2() {
        DefaultFuture<String> future = new DefaultFuture<>();

        SimpleListener<String> listener = new SimpleListener<>();

        future.setResult("Hello");

        future.setListener(listener);

        Assertions.assertTrue(listener.isFinished());
    }

    static class SimpleListener<T> implements FutureListener<T> {

        private final AtomicBoolean isFinished = new AtomicBoolean(false);

        @Override
        public void onComplete(Future<T> future) {
            isFinished.compareAndSet(false, true);
        }

        public boolean isFinished() {
            return isFinished.get();
        }
    }

}
