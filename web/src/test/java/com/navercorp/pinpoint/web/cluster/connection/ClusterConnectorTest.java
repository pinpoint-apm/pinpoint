/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.cluster.connection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ClusterConnectorTest {
    @Test
    public void testParseInetSocketAddress() {
        InetSocketAddress inetSocketAddress = ClusterConnector.parseInetSocketAddress("127.0.0.1:8080");
        Assertions.assertEquals(inetSocketAddress.getAddress().getHostAddress(), "127.0.0.1");
        Assertions.assertEquals(inetSocketAddress.getPort(), 8080);

    }

    @Test
    public void testParseInetSocketAddress_error1() {
        InetSocketAddress inetSocketAddress = ClusterConnector.parseInetSocketAddress("127.0.0.1");
        Assertions.assertNull(inetSocketAddress);
    }

    @Test
    public void testParseInetSocketAddress_error2() {
        InetSocketAddress inetSocketAddress = ClusterConnector.parseInetSocketAddress("127.0.0.1:");
        Assertions.assertNull(inetSocketAddress);

    }
}