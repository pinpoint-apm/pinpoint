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
package com.navercorp.pinpoint.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author minwoo.jung
 */
public class BatchUtils {

    private final static Logger logger = LoggerFactory.getLogger(BatchUtils.class);

    public static boolean decisionBatchServer(String ip) {
        Enumeration<NetworkInterface> interfaces;

        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.error("not found network interface", e);
            return false;
        }

        if (interfaces == null) {
            return false;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();
            Enumeration<InetAddress> inets = network.getInetAddresses();

            while (inets.hasMoreElements()) {
                InetAddress next = inets.nextElement();

                if (next instanceof Inet4Address) {
                    if (next.getHostAddress().equals(ip)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
