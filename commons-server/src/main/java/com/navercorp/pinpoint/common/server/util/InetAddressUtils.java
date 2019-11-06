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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class InetAddressUtils {
    private InetAddressUtils() {
    }

    public static List<InetAddress> toInetAddressList(List<String> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            return Collections.emptyList();
        }
        final List<InetAddress> inetAddressList = new ArrayList<InetAddress>(addressList.size());
        for (String ignoreAddress : addressList) {
            if (StringUtils.isBlank(ignoreAddress)) {
                continue;
            }
            // not throw UnknownHostException
            final InetAddress address = InetAddresses.forString(ignoreAddress);
            if (address != null) {
                inetAddressList.add(address);
            }
        }
        return inetAddressList;
    }

    public static InetAddress[] toInetAddressArray(List<String> addressList) {
        final List<InetAddress> inetList = InetAddressUtils.toInetAddressList(addressList);
        return inetList.toArray(new InetAddress[0]);
    }

}
