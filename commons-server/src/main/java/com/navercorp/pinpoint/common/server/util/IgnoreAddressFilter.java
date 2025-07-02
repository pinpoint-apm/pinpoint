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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IgnoreAddressFilter implements AddressFilter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final InetAddress[] ignoreAddressList;
    private final List<CidrAddressFilter> cidrAddressFilterList;

    public IgnoreAddressFilter(List<String> ignoreAddressList) {
        Objects.requireNonNull(ignoreAddressList, "ignoreAddressList");

        List<String> ignoreRawAddressList = new ArrayList<>(ignoreAddressList.size());
        List<CidrAddressFilter> cidrAddressFilterList = new ArrayList<>(0);
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
            return new CidrAddressFilter(address);
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

        for (CidrAddressFilter cidrAddressFilter : cidrAddressFilterList) {
            if (cidrAddressFilter.matches(address)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "IgnoreAddressFilter{" +
                "ignoreAddressList=" + Arrays.toString(ignoreAddressList) +
                ", cidrAddressFilterList=" + cidrAddressFilterList +
                '}';
    }

}
