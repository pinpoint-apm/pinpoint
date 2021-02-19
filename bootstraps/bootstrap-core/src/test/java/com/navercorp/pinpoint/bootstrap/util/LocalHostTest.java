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

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author emeroad
 */
public class LocalHostTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Ignore
    @Test
    public void portName() throws UnknownHostException, SocketException {

        logger.debug("CanonicalHostName:{}", InetAddress.getLocalHost().getCanonicalHostName());
        logger.debug("HostName:{}", InetAddress.getLocalHost().getHostName());

        logger.debug("NetworkInterface");
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            logger.debug("NetworkInterface:{}", networkInterface);
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                logger.debug(inetAddress.getCanonicalHostName());
                logger.debug(inetAddress.getHostName());
            }
        }
    }

}
