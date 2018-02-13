/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.cluster.connection;

import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.rpc.PinpointSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Taejin Koo
 */
public class CollectorClusterConnectionRepository {

    private final ConcurrentHashMap<Address, PinpointSocket> clusterConnectionRepository = new ConcurrentHashMap<>();

    public PinpointSocket putIfAbsent(Address address, PinpointSocket pinpointSocket) {
        return clusterConnectionRepository.putIfAbsent(address, pinpointSocket);
    }

    public PinpointSocket remove(Address address) {
        return clusterConnectionRepository.remove(address);
    }

    public boolean containsKey(Address address) {
        return clusterConnectionRepository.containsKey(address);
    }

    public List<Address> getAddressList() {
        // fix jdk 8 KeySetView compatibility
        Set<Address> socketAddresses = clusterConnectionRepository.keySet();
        return new ArrayList<>(socketAddresses);
    }

    public List<PinpointSocket> getClusterSocketList() {
        return new ArrayList<>(clusterConnectionRepository.values());
    }

}
