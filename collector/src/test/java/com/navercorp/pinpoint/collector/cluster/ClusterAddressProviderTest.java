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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.collector.util.MultipleAddress;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ClusterAddressProviderTest {

    @Test
    public void clusterAddressProviderTest() {
        Address multipleAddress = createMultipleAddress();
        ClusterAddressProvider multiAddressProvider = new ClusterAddressProvider(multipleAddress);

        InetSocketAddress prev = null;
        for (int i = 0; i < 100; i++) {
            InetSocketAddress current = multiAddressProvider.resolve();

            if (current == null || current.equals(prev)) {
                Assert.fail();
            }
            prev = current;
        }
    }

    private Address createMultipleAddress() {
        List<String> hostAddresses = Arrays.asList("127.0.0.0", "127.0.0.1", "0.0.0.0");
        return new MultipleAddress(hostAddresses, 9994);
    }

    @Test(expected = NullPointerException.class)
    public void failedToCreateTest() {
        ClusterAddressProvider multiAddressProvider = new ClusterAddressProvider(null);
    }

}
