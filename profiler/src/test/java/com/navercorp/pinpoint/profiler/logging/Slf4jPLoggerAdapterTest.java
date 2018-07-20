/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.logging;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class Slf4jPLoggerAdapterTest {

    @Test
    public void getSimpleName() throws Exception {
        assertEquals("int[]", Slf4jPLoggerAdapter.getSimpleName((new int[1]).getClass()));
        assertEquals("Slf4jPLoggerAdapterTest$Dummy", Slf4jPLoggerAdapter.getSimpleName(Dummy.class));

        Runnable r = new Runnable() {
            @Override
            public void run() {
            }
        };
        assertEquals("Slf4jPLoggerAdapterTest$1", Slf4jPLoggerAdapter.getSimpleName(r.getClass()));
    }

    @Test
    public void isSimpletype() {
        assertTrue(Slf4jPLoggerAdapter.isSimpleType(new Integer(1)));
        assertTrue(Slf4jPLoggerAdapter.isSimpleType(Boolean.TRUE));

        // array, object
        assertFalse(Slf4jPLoggerAdapter.isSimpleType(new int[1]));
        assertFalse(Slf4jPLoggerAdapter.isSimpleType(new Dummy()));
    }

    private class Dummy {
    }

}