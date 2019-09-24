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

package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultChannelFactoryBuilder implements ChannelFactoryBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String factoryName;

    private int executorQueueSize = 1024;
    private HeaderFactory headerFactory;

    private ClientOption clientOption;

    private final LinkedList<ClientInterceptor> clientInterceptorList = new LinkedList<ClientInterceptor>();
    private NameResolverProvider nameResolverProvider;

    public DefaultChannelFactoryBuilder(String factoryName) {
        this.factoryName = Assert.requireNonNull(factoryName, "factoryName");
    }

    @Override
    public void setExecutorQueueSize(int executorQueueSize) {
        Assert.isTrue(executorQueueSize > 0, "must be `executorQueueSize > 0`");
        this.executorQueueSize = executorQueueSize;
    }

    @Override
    public void setHeaderFactory(HeaderFactory headerFactory) {
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory");
    }

    @Override
    public void addFirstClientInterceptor(ClientInterceptor clientInterceptor) {
        Assert.requireNonNull(clientInterceptor, "clientInterceptor");
        this.clientInterceptorList.addFirst(clientInterceptor);
    }

    @Override
    public void addClientInterceptor(ClientInterceptor clientInterceptor) {
        Assert.requireNonNull(clientInterceptor, "clientInterceptor");
        this.clientInterceptorList.add(clientInterceptor);
    }

    @Override
    public void setClientOption(ClientOption clientOption) {
        this.clientOption = Assert.requireNonNull(clientOption, "clientOption");
    }

    @Override
    public void setNameResolverProvider(NameResolverProvider nameResolverProvider) {
        this.nameResolverProvider = Assert.requireNonNull(nameResolverProvider, "nameResolverProvider");
    }

    @Override
    public ChannelFactory build() {
        logger.info("build ChannelFactory:{}", factoryName);
        Assert.requireNonNull(headerFactory, "headerFactory");
        Assert.requireNonNull(clientOption, "clientOption");

        return new DefaultChannelFactory(factoryName, executorQueueSize,
                headerFactory, nameResolverProvider,
                clientOption, clientInterceptorList);
    }
}
