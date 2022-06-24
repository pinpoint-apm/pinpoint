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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class PinpointByteArrayOutputStreamTest {

    private final String TEST_STRING = "hello pinpoint";

    @Test
    public void test1() throws IOException {
        PinpointByteArrayOutputStream pbaos = new PinpointByteArrayOutputStream(8);

        pbaos.write(TEST_STRING.getBytes());

        Assertions.assertEquals(TEST_STRING, pbaos.toString());
    }

    @Test
    public void test2() throws IOException {
        Assertions.assertThrows(BufferOverflowException.class, () -> {
            PinpointByteArrayOutputStream pbaos = new PinpointByteArrayOutputStream(8, false);

            pbaos.write(TEST_STRING.getBytes());
        });
    }

}
