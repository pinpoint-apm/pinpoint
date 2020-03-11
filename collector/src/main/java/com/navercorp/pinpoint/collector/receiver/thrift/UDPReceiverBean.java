/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.thrift;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.udp.BaseUDPHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.thrift.udp.NetworkAvailabilityCheckPacketFilter;
import com.navercorp.pinpoint.collector.receiver.thrift.udp.PacketHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.thrift.udp.ReusePortSocketOptionHolder;
import com.navercorp.pinpoint.collector.receiver.thrift.udp.TBaseFilter;
import com.navercorp.pinpoint.collector.receiver.thrift.udp.TBaseFilterChain;
import com.navercorp.pinpoint.collector.receiver.thrift.udp.UDPReceiver;
import com.navercorp.pinpoint.collector.util.DatagramPacketFactory;
import com.navercorp.pinpoint.collector.util.DefaultObjectPool;
import com.navercorp.pinpoint.collector.util.ObjectPool;
import com.navercorp.pinpoint.collector.util.ObjectPoolFactory;
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
    private boolean reusePort = false;
    private int socketCount = -1;

    private UDPReceiver udpReceiver;
    private Executor executor;

    private DispatchHandler dispatchHandler;
    private AddressFilter addressFilter;
    private int datagramPoolSize = 1024 * 4;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enable) {
            return;
        }
        Objects.requireNonNull(beanName, "beanName");
        Objects.requireNonNull(bindIp, "bindIp");
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        Objects.requireNonNull(addressFilter, "addressFilter");
        Objects.requireNonNull(executor, "executor");

        udpReceiver = createUdpReceiver(beanName, this.bindIp, bindPort, udpBufferSize, executor, dispatchHandler, addressFilter);
        udpReceiver.start();
    }


    private UDPReceiver createUdpReceiver(String name, String bindIp, int port, int udpBufferSize, Executor executor, DispatchHandler dispatchHandler, AddressFilter ignoreAddressFilter) {
        TBaseFilterChain filterChain = newTBaseFilterChain();
        @SuppressWarnings("unchecked")
        PacketHandlerFactory<DatagramPacket> packetHandlerFactory = new BaseUDPHandlerFactory<DatagramPacket>(dispatchHandler, filterChain, ignoreAddressFilter);

        InetSocketAddress bindAddress = new InetSocketAddress(bindIp, port);

        ObjectPoolFactory<DatagramPacket> packetFactory = new DatagramPacketFactory();
        ObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(packetFactory, datagramPoolSize);

        if (reusePort) {
            ReusePortSocketOptionHolder reusePortSocketOption = ReusePortSocketOptionHolder.create(socketCount);
            return new UDPReceiver(name, packetHandlerFactory, executor, udpBufferSize, bindAddress, reusePortSocketOption, pool);
        } else {
            return new UDPReceiver(name, packetHandlerFactory, executor, udpBufferSize, bindAddress, pool);
        }
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
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
    }

    public void setAddressFilter(AddressFilter addressFilter) {
        this.addressFilter = Objects.requireNonNull(addressFilter, "addressFilter");
    }

    public void setBindIp(String bindIp) {
        this.bindIp = Objects.requireNonNull(bindIp, "bindIp");
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public void setUdpBufferSize(int udpBufferSize) {
        this.udpBufferSize = udpBufferSize;
    }

    public void setReusePort(boolean reusePort) {
        this.reusePort = reusePort;
    }

    public void setSocketCount(int socketCount) {
        this.socketCount = socketCount;
    }

    public void setDatagramPoolSize(int datagramPoolSize) {
        this.datagramPoolSize = datagramPoolSize;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
