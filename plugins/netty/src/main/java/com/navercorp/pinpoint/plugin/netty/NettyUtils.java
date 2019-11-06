/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.netty;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Taejin Koo
 */
public final class NettyUtils {

    //    InetSocketAddress does not allow both address and hostName fields to be null.
    //    For this reason, we first use address field that does not look up dns, if address field is null then we use hostName field.
    public static String getEndPoint(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            final InetAddress remoteAddress = inetSocketAddress.getAddress();
            if (remoteAddress != null) {
                return HostAndPort.toHostAndPortString(remoteAddress.getHostAddress(), inetSocketAddress.getPort());
            }
            // Warning : InetSocketAddressAvoid unnecessary DNS lookup  (warning:InetSocketAddress.getHostName())
            final String hostName = inetSocketAddress.getHostName();
            if (hostName != null) {
                return HostAndPort.toHostAndPortString(hostName, inetSocketAddress.getPort());
            }
        }

        return NettyConstants.UNKNOWN_ADDRESS;
    }

}
