/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public class CidrAddressFilter {

    private final String ipAddress;
    private final int cidrPrefix;
    private final IpSubnetFilterRule ipSubnetFilterRule;

    CidrAddressFilter(String ipAddress, int cidrPrefix) {
        this.ipAddress = ipAddress;
        this.cidrPrefix = cidrPrefix;

        this.ipSubnetFilterRule = new IpSubnetFilterRule(ipAddress, cidrPrefix, IpFilterRuleType.ACCEPT);
    }

    boolean matches(InetSocketAddress remoteAddress) {
        return ipSubnetFilterRule.matches(remoteAddress);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CidrAddressFilter{");
        sb.append("address='").append(ipAddress).append('/').append(cidrPrefix);;
        sb.append('}');
        return sb.toString();
    }

}
