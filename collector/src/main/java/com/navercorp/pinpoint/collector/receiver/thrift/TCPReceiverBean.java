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
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.DefaultTCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.DefaultTCPReceiver;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPReceiver;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TCPReceiverBean implements InitializingBean, DisposableBean, BeanNameAware {
    private String beanName;

    private boolean enable = true;

    private String bindIp;
    private int bindPort;

    private TCPReceiver tcpReceiver;
    private Executor executor;

    private PinpointServerAcceptorProvider acceptorProvider;

    private DispatchHandler dispatchHandler;

    private TCPPacketHandlerFactory tcpPacketHandlerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enable) {
            return;
        }
        Objects.requireNonNull(beanName, "beanName");
        Objects.requireNonNull(bindIp, "bindIp");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        Objects.requireNonNull(acceptorProvider, "acceptorProvider");

        tcpReceiver = createTcpReceiver(beanName, this.bindIp, bindPort, executor, dispatchHandler, this.tcpPacketHandlerFactory, acceptorProvider);
        tcpReceiver.start();
    }

    protected TCPReceiver createTcpReceiver(String beanName, String bindIp, int port, Executor executor,
                                                 DispatchHandler dispatchHandler, TCPPacketHandlerFactory tcpPacketHandlerFactory, PinpointServerAcceptorProvider acceptorProvider) {
        InetSocketAddress bindAddress = new InetSocketAddress(bindIp, port);
        TCPPacketHandler tcpPacketHandler = wrapDispatchHandler(dispatchHandler, tcpPacketHandlerFactory);

        return new DefaultTCPReceiver(beanName, tcpPacketHandler, executor, bindAddress, acceptorProvider);
    }

    private TCPPacketHandler wrapDispatchHandler(DispatchHandler dispatchHandler, TCPPacketHandlerFactory tcpPacketHandlerFactory) {
        if (tcpPacketHandlerFactory == null) {
            // using default Factory
            tcpPacketHandlerFactory = new DefaultTCPPacketHandlerFactory();
        }
        return tcpPacketHandlerFactory.build(dispatchHandler);
    }


    @Override
    public void destroy() throws Exception {
        if (!enable) {
            return;
        }
        if (tcpReceiver != null) {
            tcpReceiver.shutdown();
        }
    }

    public void setExecutor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }

    public void setTcpPacketHandlerFactory(TCPPacketHandlerFactory tcpPacketHandlerFactory) {
        this.tcpPacketHandlerFactory = tcpPacketHandlerFactory;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = Objects.requireNonNull(bindIp, "bindIp");
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }


    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setAcceptorProvider(PinpointServerAcceptorProvider acceptorProvider) {
        this.acceptorProvider = acceptorProvider;
    }

}
