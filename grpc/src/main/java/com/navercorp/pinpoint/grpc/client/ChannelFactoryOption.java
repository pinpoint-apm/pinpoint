/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.common.util.Assert;

import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ChannelFactoryOption {

    private final String name;

    private final int executorQueueSize;

    private final HeaderFactory headerFactory;

    private final NameResolverProvider nameResolverProvider;

    private final List<ClientInterceptor> clientInterceptorList;

    private final ClientOption clientOption;

    public String getName() {
        return name;
    }

    public int getExecutorQueueSize() {
        return executorQueueSize;
    }

    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }

    @Nullable
    public NameResolverProvider getNameResolverProvider() {
        return nameResolverProvider;
    }

    public List<ClientInterceptor> getClientInterceptorList() {
        return clientInterceptorList;
    }

    public ClientOption getClientOption() {
        return clientOption;
    }

    private ChannelFactoryOption(String name, int executorQueueSize, HeaderFactory headerFactory, NameResolverProvider nameResolverProvider, List<ClientInterceptor> clientInterceptorList, ClientOption clientOption) {
        this.name = Assert.requireNonNull(name, "name must not be null");

        Assert.isTrue(executorQueueSize > 0, "must be `executorQueueSize > 0`");
        this.executorQueueSize = executorQueueSize;

        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory must not be null");

        this.nameResolverProvider = nameResolverProvider;

        this.clientInterceptorList = Assert.requireNonNull(clientInterceptorList, "clientInterceptorList must not be null");
        this.clientOption = Assert.requireNonNull(clientOption, "clientOption must not be null");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelFactoryOption{");
        sb.append("name='").append(name).append('\'');
        sb.append(", executorQueueSize=").append(executorQueueSize);
        sb.append(", headerFactory=").append(headerFactory);
        sb.append(", nameResolverProvider=").append(nameResolverProvider);
        sb.append(", clientInterceptorList=").append(clientInterceptorList);
        sb.append(", clientOption=").append(clientOption);
        sb.append('}');
        return sb.toString();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private int executorQueueSize = 1024;
        private HeaderFactory headerFactory;
        private NameResolverProvider nameResolverProvider;
        private List<ClientInterceptor> clientInterceptorList = new ArrayList<ClientInterceptor>();
        private ClientOption clientOption = new ClientOption.Builder().build();

        public ChannelFactoryOption build() {
            final ChannelFactoryOption channelFactoryOption = new ChannelFactoryOption(name, executorQueueSize, headerFactory, nameResolverProvider, clientInterceptorList, clientOption);
            return channelFactoryOption;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setExecutorQueueSize(int executorQueueSize) {
            this.executorQueueSize = executorQueueSize;
        }

        public void setHeaderFactory(HeaderFactory headerFactory) {
            this.headerFactory = headerFactory;
        }

        public void setNameResolverProvider(NameResolverProvider nameResolverProvider) {
            this.nameResolverProvider = nameResolverProvider;
        }

        public void addClientInterceptor(ClientInterceptor clientInterceptor) {
            Assert.requireNonNull(clientInterceptor, "clientInterceptor must not be null");
            this.clientInterceptorList.add(clientInterceptor);
        }

        public void setClientOption(ClientOption clientOption) {
            this.clientOption = clientOption;
        }
    }
}