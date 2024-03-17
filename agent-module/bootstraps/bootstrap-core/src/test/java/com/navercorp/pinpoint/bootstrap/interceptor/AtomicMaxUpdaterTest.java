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

package com.navercorp.pinpoint.bootstrap.interceptor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class AtomicMaxUpdaterTest {
    @Test
    public void update() {
        AtomicMaxUpdater updater = new AtomicMaxUpdater();

        Assertions.assertFalse(updater.update(0));


        Assertions.assertTrue(updater.update(1));

        Assertions.assertTrue(updater.update(10));

        Assertions.assertFalse(updater.update(5));
        Assertions.assertEquals(updater.getIndex(), 10);


    }
}
