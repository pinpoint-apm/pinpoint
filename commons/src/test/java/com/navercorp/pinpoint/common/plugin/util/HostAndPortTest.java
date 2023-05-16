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

package com.navercorp.pinpoint.common.plugin.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HostAndPortTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testToHostAndPortString() {
        String hostAndPortString = HostAndPort.toHostAndPortString("127.0.1.1", 80);
        Assertions.assertEquals("127.0.1.1:80", hostAndPortString);
    }

    @Test
    public void testToHostAndPortString_defaultPort() {
        String hostAndPortString = HostAndPort.toHostAndPortString("127.0.1.1", HostAndPort.NO_PORT);
        Assertions.assertEquals("127.0.1.1", hostAndPortString);
    }


    @Test
    public void testIsValidPort() {

        Assertions.assertTrue(HostAndPort.isValidPort(0));
        Assertions.assertTrue(HostAndPort.isValidPort(8080));
        Assertions.assertTrue(HostAndPort.isValidPort(65535));

        Assertions.assertFalse(HostAndPort.isValidPort(-1));
    }

    @Test
    public void testGetPortOrNoPort() {

        Assertions.assertEquals(HostAndPort.getPortOrNoPort(1), 1);

        Assertions.assertEquals(HostAndPort.getPortOrNoPort(-65535), -1);
        Assertions.assertEquals(HostAndPort.getPortOrNoPort(-1), -1);
    }

    @Test
    public void testGetValidPortOrNoPort() {

        Assertions.assertEquals(HostAndPort.getValidPortOrNoPort(70000), -1);

        Assertions.assertEquals(HostAndPort.getValidPortOrNoPort(-65535), -1);
        Assertions.assertEquals(HostAndPort.getValidPortOrNoPort(-1), -1);
    }
}