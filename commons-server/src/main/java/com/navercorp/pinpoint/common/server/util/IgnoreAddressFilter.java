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

package com.navercorp.pinpoint.common.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IgnoreAddressFilter implements AddressFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InetAddress[] ignoreAddressList;
    private final List<CidrAddressFilter> cidrAddressFilterList;

    public IgnoreAddressFilter(List<String> ignoreAddressList) {
        Objects.requireNonNull(ignoreAddressList, "ignoreAddressList");

        List<String> ignoreRawAddressList = new ArrayList<>(ignoreAddressList.size());
        List<CidrAddressFilter> cidrAddressFilterList = new ArrayList<CidrAddressFilter>(0);
        for (String ignoreAddress : ignoreAddressList) {
            if (isCidrAddress(ignoreAddress)) {
                CidrAddressFilter cidrAddressFilter = createCidrAddressFilter(ignoreAddress);
                if (cidrAddressFilter != null) {
                    cidrAddressFilterList.add(cidrAddressFilter);
                }
            } else {
                ignoreRawAddressList.add(ignoreAddress);
            }
        }

        this.ignoreAddressList = InetAddressUtils.toInetAddressArray(ignoreRawAddressList);
        this.cidrAddressFilterList = cidrAddressFilterList;
    }

    private boolean isCidrAddress(String address) {
        return address.contains("/");
    }

    private CidrAddressFilter createCidrAddressFilter(String address) {
        try {
            String[] cidrAddress = address.split("/", 2);
            String ipAddress = cidrAddress[0];
            int cidrPrefix = Integer.parseInt(cidrAddress[1]);

            return new CidrAddressFilter(ipAddress, cidrPrefix);
        } catch (Exception e) {
            logger.warn("Failed to create CidrAddress:{}. message:{}", address, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean accept(InetAddress address) {

        for (InetAddress ignore : ignoreAddressList) {
            if (ignore.equals(address)) {
                return false;
            }
        }

        if (cidrAddressFilterList.isEmpty()) {
            return true;
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, 0);
        for (CidrAddressFilter cidrAddressFilter : cidrAddressFilterList) {
            if (cidrAddressFilter.matches(inetSocketAddress)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IgnoreAddressFilter{");
        sb.append("ignoreAddressList=").append(Arrays.toString(ignoreAddressList));
        sb.append(", cidrAddressFilterList=").append(cidrAddressFilterList);
        sb.append('}');
        return sb.toString();
    }

}
