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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author jaehong.kim
 */
public class Log4j2PLoggerAdapterTest {

    @Test
    public void getSimpleName() {
        assertEquals("int[]", Log4j2PLoggerAdapter.getSimpleName((new int[1]).getClass()));
        assertEquals("Log4j2PLoggerAdapterTest$Dummy", Log4j2PLoggerAdapter.getSimpleName(Dummy.class));

        Runnable r = new Runnable() {
            @Override
            public void run() {
            }
        };
        assertEquals("Log4j2PLoggerAdapterTest$1", Log4j2PLoggerAdapter.getSimpleName(r.getClass()));
    }

    @Test
    public void isSimpleType() {
        assertTrue(Log4j2PLoggerAdapter.isSimpleType(new Integer(1)));
        assertTrue(Log4j2PLoggerAdapter.isSimpleType(Boolean.TRUE));

        // array, object
        assertFalse(Log4j2PLoggerAdapter.isSimpleType(new int[1]));
        assertFalse(Log4j2PLoggerAdapter.isSimpleType(new Dummy()));
    }

    private class Dummy {
    }

}