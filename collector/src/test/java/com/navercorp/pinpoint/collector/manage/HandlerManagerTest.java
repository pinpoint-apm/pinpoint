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

package com.navercorp.pinpoint.collector.manage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Taejin Koo
 */
public class HandlerManagerTest {

    @Test
    public void onOffTest() {
        HandlerManager handlerManager = new HandlerManager();
        Assertions.assertTrue(handlerManager.isEnable());

        handlerManager.disableAccess();
        Assertions.assertFalse(handlerManager.isEnable());

        handlerManager.enableAccess();
        Assertions.assertTrue(handlerManager.isEnable());
    }

    @Test
    public void getNameTest() {
        HandlerManager handlerManager = new HandlerManager();
        String name = handlerManager.getName();

        Assertions.assertEquals("HandlerManager", name);
    }

}
