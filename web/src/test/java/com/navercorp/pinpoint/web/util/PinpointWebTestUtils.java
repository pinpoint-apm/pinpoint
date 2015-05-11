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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.NetUtils;

/**
 * @author Taejin Koo
 */
public class PinpointWebTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(PinpointWebTestUtils.class);

    private PinpointWebTestUtils() {
    }

    public static int findAvailablePort() throws IOException {
        return findAvailablePort(21111);
    }

    public static int findAvailablePort(int defaultPort) throws IOException {
        int bindPort = defaultPort;

        ServerSocket serverSocket = null;
        while (0xFFFF >= bindPort && serverSocket == null) {
            try {
                serverSocket = new ServerSocket(bindPort);
            } catch (IOException ex) {
                bindPort++;
            }
        }
        
        if (serverSocket != null) {
            serverSocket.close();
            return bindPort;
        } 
        
        throw new IOException("can't find available port.");
    }
    
    public static String getRepresentationLocalV4Ip() {
        String ip = NetUtils.getLocalV4Ip();

        if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
            return ip;
        }

        // local ip addresses with all LOOPBACK addresses removed
        List<String> ipList = NetUtils.getLocalV4IpList();
        if (ipList.size() > 0) {
            return ipList.get(0);
        }

        return NetUtils.LOOPBACK_ADDRESS_V4;
    }
    
}
