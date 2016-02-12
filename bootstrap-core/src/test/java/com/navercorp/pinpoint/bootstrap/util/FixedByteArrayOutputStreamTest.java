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
package com.navercorp.pinpoint.bootstrap.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author minwoo.jung
 */
public class FixedByteArrayOutputStreamTest {

    @Test
    public void test() throws IOException {
        FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(10);
        
        byte[] temp = new byte[5];
        Arrays.fill(temp, (byte)5);
        outStream.write(temp);
        assertArrayEquals(outStream.toByteArray(), temp);

        byte[] temp2 = new byte[10];
        Arrays.fill(temp2, (byte)10);
        outStream.write(temp2);
        byte[] result = new byte[] {5, 5, 5, 5, 5, 10, 10, 10, 10, 10};
        assertArrayEquals(outStream.toByteArray(), result);
        
        byte[] temp3 = new byte[10];
        Arrays.fill(temp3, (byte)10);
        outStream.write(temp3);
        assertArrayEquals(outStream.toByteArray(), result);
        
        outStream.close();
    }

    @Test
    public void test2() throws IOException {
        FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(10);
        
        byte[] temp = new byte[9];
        Arrays.fill(temp, (byte)9);
        outStream.write(temp);
        assertArrayEquals(outStream.toByteArray(), temp);

        byte[] temp2 = new byte[10];
        Arrays.fill(temp2, (byte)10);
        outStream.write(temp2);
        byte[] result = new byte[] {9, 9, 9, 9, 9, 9, 9, 9, 9, 10};
        assertArrayEquals(outStream.toByteArray(), result);
        
        byte[] temp3 = new byte[10];
        Arrays.fill(temp3, (byte)11);
        outStream.write(temp3);
        assertArrayEquals(outStream.toByteArray(), result);
        
        outStream.close();
            
    }
}
