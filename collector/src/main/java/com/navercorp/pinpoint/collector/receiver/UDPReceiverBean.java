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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.config.DataReceiverGroupConfiguration;
import com.navercorp.pinpoint.collector.receiver.tcp.TCPReceiver;
import com.navercorp.pinpoint.collector.receiver.udp.BaseUDPHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.udp.NetworkAvailabilityCheckPacketFilter;
import com.navercorp.pinpoint.collector.receiver.udp.PacketHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.udp.TBaseFilter;
import com.navercorp.pinpoint.collector.receiver.udp.TBaseFilterChain;
import com.navercorp.pinpoint.collector.receiver.udp.UDPReceiver;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class UDPReceiverBean implements InitializingBean, DisposableBean, BeanNameAware {

    private String beanName;

    private boolean enable = true;

    private String bindIp;
    private int bindPort;
    private int udpBufferSize;

    private UDPReceiver udpReceiver;
    private Executor executor;

    private DispatchHandler dispatchHandler;
    private AddressFilter addressFilter;



    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enable) {
            return;
        }
        Objects.requireNonNull(beanName, "beanName must not be null");
        Objects.requireNonNull(bindIp, "bindIp must not be null");
        Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        Objects.requireNonNull(addressFilter, "addressFilter must not be null");
        Objects.requireNonNull(executor, "executor must not be null");

        udpReceiver = createUdpReceiver(beanName, this.bindIp, bindPort, udpBufferSize, executor, dispatchHandler, addressFilter);
        udpReceiver.start();
    }


    private UDPReceiver createUdpReceiver(String name, String bindIp, int port, int udpBufferSize, Executor executor, DispatchHandler dispatchHandler, AddressFilter ignoreAddressFilter) {
        TBaseFilterChain filterChain = newTBaseFilterChain();
        @SuppressWarnings("unchecked")
        PacketHandlerFactory<DatagramPacket> packetHandlerFactory = new BaseUDPHandlerFactory<DatagramPacket>(dispatchHandler, filterChain, ignoreAddressFilter);

        InetSocketAddress bindAddress = new InetSocketAddress(bindIp, port);
        return new UDPReceiver(name, packetHandlerFactory, executor, udpBufferSize, bindAddress);
    }


    private TBaseFilterChain newTBaseFilterChain() {
        List<TBaseFilter> tBaseFilters = Collections.singletonList(new NetworkAvailabilityCheckPacketFilter());
        @SuppressWarnings("unchecked")
        TBaseFilterChain tBaseFilterChain = new TBaseFilterChain(tBaseFilters);
        return tBaseFilterChain;
    }

    @Override
    public void destroy() throws Exception {
        if (!enable) {
            return;
        }
        if (udpReceiver != null) {
            udpReceiver.shutdown();
        }
    }

    public void setExecutor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
    }

    public void setAddressFilter(AddressFilter addressFilter) {
        this.addressFilter = Objects.requireNonNull(addressFilter, "addressFilter must not be null");
    }

    public void setBindIp(String bindIp) {
        this.bindIp = Objects.requireNonNull(bindIp, "bindIp must not be null");
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public void setUdpBufferSize(int udpBufferSize) {
        this.udpBufferSize = udpBufferSize;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
