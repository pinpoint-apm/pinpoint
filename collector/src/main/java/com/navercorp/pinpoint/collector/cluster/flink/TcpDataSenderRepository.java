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
package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.sender.FlinkTcpDataSender;
import com.navercorp.pinpoint.collector.service.SendDataToFlinkService;
import com.navercorp.pinpoint.collector.util.Address;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public class TcpDataSenderRepository {
    private final ConcurrentHashMap<Address, SenderContext> clusterConnectionRepository = new ConcurrentHashMap<>();
    private final SendDataToFlinkService sendDataToFlinkService;

    TcpDataSenderRepository(SendDataToFlinkService sendDataToFlinkService) {
        this.sendDataToFlinkService = sendDataToFlinkService;
    }

    public SenderContext putIfAbsent(Address address, SenderContext senderContext) {
        SenderContext context =  clusterConnectionRepository.putIfAbsent(address, senderContext);
        replaceDataInSendDataToFlinkService();
        return context;
    }

    public SenderContext remove(Address address) {
        SenderContext senderContext = clusterConnectionRepository.remove(address);
        replaceDataInSendDataToFlinkService();
        return senderContext;
    }

    private void replaceDataInSendDataToFlinkService() {
        Collection<SenderContext> values = clusterConnectionRepository.values();

        List<FlinkTcpDataSender> tcpDataSenderList = new ArrayList<>(values.size());
        for (SenderContext senderContext : values) {
            tcpDataSenderList.add(senderContext.getFlinkTcpDataSender());
        }

        sendDataToFlinkService.replaceFlinkTcpDataSenderList(tcpDataSenderList);
    }

    public boolean containsKey(Address address) {
        return clusterConnectionRepository.containsKey(address);
    }

    public List<Address> getAddressList() {
        Set<Address> socketAddresses = clusterConnectionRepository.keySet();
        return new ArrayList<>(socketAddresses);
    }

    public List<SenderContext> getClusterSocketList() {
        return new ArrayList<>(clusterConnectionRepository.values());
    }
}
