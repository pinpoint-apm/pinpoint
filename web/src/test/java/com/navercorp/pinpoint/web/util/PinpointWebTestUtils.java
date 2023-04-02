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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.util.NetUtils;

import java.net.InetAddress;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class PinpointWebTestUtils {


    private PinpointWebTestUtils() {
    }

    public static String getRepresentationLocalV4Ip() {
        String ip = NetUtils.getLocalV4Ip();

        if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
            return ip;
        }

        // local ip addresses with all LOOPBACK addresses removed
        final List<String> ipList = NetUtils.getLocalV4IpList();
        return findReachableIp(ipList);
    }

    private static String findReachableIp(List<String> ips) {
        for (final String ip: ips) {
            if (isReachable(ip)) {
                return ip;
            }
        }
        return NetUtils.LOOPBACK_ADDRESS_V4;
    }

    private static boolean isReachable(String ip) {
        try {
            return InetAddress.getByName(ip).isReachable(3);
        } catch (Throwable ignored) {
            return false;
        }
    }
    
}
