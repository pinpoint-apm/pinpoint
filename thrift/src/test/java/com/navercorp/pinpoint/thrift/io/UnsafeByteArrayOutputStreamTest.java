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

package com.navercorp.pinpoint.thrift.io;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Taejin Koo
 */
public class UnsafeByteArrayOutputStreamTest {
    
    private final String TEST_STRING1 = "hello pinpoint";
    private final String TEST_STRING2 = "hi pinpoint";

    @Test
    public void test1() throws IOException {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream(32);

        ubaos.write(TEST_STRING1.getBytes());
        Assert.assertEquals(TEST_STRING1, ubaos.toString());

        ubaos.reset();

        ubaos.write(TEST_STRING2.getBytes());
        Assert.assertEquals(TEST_STRING2, ubaos.toString());

        byte[] data = ubaos.toByteArray();
        Assert.assertEquals(TEST_STRING2 + "int", new String(data).trim());
    }
    
    @Test (expected = BufferOverflowException.class)
    public void test2() throws IOException {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream(8, false);

        ubaos.write(TEST_STRING1.getBytes());
    }
    
}
