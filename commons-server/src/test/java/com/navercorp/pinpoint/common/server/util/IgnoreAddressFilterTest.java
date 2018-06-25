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

import org.apache.hadoop.hbase.shaded.com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IgnoreAddressFilterTest {

    private final List<String> ignoreList = Arrays.asList("10.0.0.1", "10.0.0.2");
    private final InetAddress success = InetAddresses.forString("100.0.0.0");


    @Test
    public void accept() {
        List<InetAddress> inetAddresses = InetAddressUtils.toInetAddressList(ignoreList);


        AddressFilter filter = new IgnoreAddressFilter(ignoreList);
        for (InetAddress inetAddress : inetAddresses) {
            Assert.assertFalse(filter.accept(inetAddress));
        }

        Assert.assertTrue(filter.accept(success));
        Assert.assertTrue(filter.accept(null));
    }
}