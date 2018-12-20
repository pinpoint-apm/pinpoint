/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServerFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int port;
    private final List<BindableService> bindableServices = new ArrayList<BindableService>();

    public ServerFactory(int port) {
        this.port = port;
    }

    public void addService(BindableService bindableService) {
        Assert.requireNonNull(bindableService, "bindableService must not be null");
        this.bindableServices.add(bindableService);
    }

    public Server build() {
        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
        for (BindableService bindableService : this.bindableServices) {
            serverBuilder.addService(bindableService);
        }

        HeaderFactory<AgentHeaderFactory.Header> headerFactory = new AgentHeaderFactory();
        HeaderPropagationInterceptor<AgentHeaderFactory.Header> headerContext = new HeaderPropagationInterceptor<AgentHeaderFactory.Header>(headerFactory, AgentInfoContext.agentInfoKey);
        serverBuilder.intercept(headerContext);
        Server server = serverBuilder.build();
        return server;
    }
}
