/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.common.util.NetUtils;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AddressParser {

    private static final NetUtils.HostAndPortFactory<Address> addressFactory = new NetUtils.HostAndPortFactory<Address>() {
        @Override
        public Address newInstance(String host, int port) {
            return new DefaultAddress(host, port);
        }
    };

    public static Address parseAddress(String address) {
        return NetUtils.parseHostAndPort(address, addressFactory);
    }

    public static List<Address> parseAddressLIst(List<String> addressList) {
        return NetUtils.toHostAndPortLIst(addressList, addressFactory);
    }

}
