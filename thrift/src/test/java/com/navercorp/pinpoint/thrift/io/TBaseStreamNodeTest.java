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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TBaseStreamNodeTest {

    @Test
    public void size() throws Exception {
        final byte[] buf = "foo".getBytes();

        UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
        ByteArrayOutputStreamTransport transport = new ByteArrayOutputStreamTransport(out);
        
        out.write(buf);
        TBaseStreamNode node = new TBaseStreamNode(transport);

        node.setBeginPosition(0);
        node.setEndPosition(buf.length);

        assertEquals(buf.length, node.size());

        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        node.writeTo(copy);

        Arrays.equals(buf, copy.toByteArray());
    }
}