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

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IgnoreAddressFilterTest {

    private final List<String> ignoreList = Arrays.asList("10.0.0.1", "10.0.0.2", "11.0.0.1");
    private final List<String> successList = Arrays.asList("100.0.0.0", "10.0.1.1", "11.0.0.2");

    @Test
    public void acceptTest() {
        final List<InetAddress> ignoreAddresses = InetAddressUtils.toInetAddressList(ignoreList);

        List<String> copiedSuccessList = new ArrayList(successList);
        copiedSuccessList.add("10.0.0.3");
        final List<InetAddress> successAddresses = InetAddressUtils.toInetAddressList(copiedSuccessList);

        final List<String> ignoreListForFilter = Arrays.asList("10.0.0.1", "10.0.0.2", "11.0.0.1");

        assertAddressFilter(ignoreListForFilter, ignoreAddresses, successAddresses);
    }

    @Test
    public void acceptWithCidrAddressTest() {
        final List<InetAddress> ignoreAddresses = InetAddressUtils.toInetAddressList(ignoreList);
        final List<InetAddress> successAddresses = InetAddressUtils.toInetAddressList(successList);

        final List<String> ignoreListForFilter = Arrays.asList("10.0.0.0/24", "11.0.0.1");

        assertAddressFilter(ignoreListForFilter, ignoreAddresses, successAddresses);
    }

    private void assertAddressFilter(List<String> ignoreFilterAddressList, List<InetAddress> ignoreAddresses, List<InetAddress> successAddresses) {
        AddressFilter filter = new IgnoreAddressFilter(ignoreFilterAddressList);
        for (InetAddress ignoreAddress : ignoreAddresses) {
            Assert.assertFalse(filter.accept(ignoreAddress));
        }

        for (InetAddress successAddress : successAddresses) {
            Assert.assertTrue(filter.accept(successAddress));
        }

        Assert.assertTrue(filter.accept(null));
    }

}