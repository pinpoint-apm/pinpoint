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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;

import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class AbstractServerServiceFactory implements FactoryBean<ServerServiceDefinition>, InitializingBean {

    protected DispatchHandler dispatchHandler;
    // @Nullable for test
    protected ServerInterceptor serverInterceptor;

    protected ServerRequestFactory serverRequestFactory;

    public AbstractServerServiceFactory() {
        // circular reference workaround
//        this.dispatchHandlerFactory = Objects.requireNonNull(dispatchHandlerFactory, "dispatchHandlerFactory");
//        this.serverInterceptor = serverInterceptor;
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }

    public void setServerInterceptor(ServerInterceptor serverInterceptor) {
        this.serverInterceptor = serverInterceptor;
    }

    public void setServerRequestFactory(ServerRequestFactory serverRequestFactory) {
        this.serverRequestFactory = serverRequestFactory;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
    }

    @Override
    public ServerServiceDefinition getObject() throws Exception {
        // WARNING singleton
        // final ServerInterceptor interceptor = FactoryBean<ServerInterceptor>.getObject();
        final ServerInterceptor interceptor = serverInterceptor;
        if (interceptor == null) {
            return newServerServiceDefinition();
        }
        final ServerServiceDefinition serverServiceDefinition = newServerServiceDefinition();
        return ServerInterceptors.intercept(serverServiceDefinition, interceptor);
    }

    abstract ServerServiceDefinition newServerServiceDefinition();

    @Override
    public Class<ServerServiceDefinition> getObjectType() {
        return ServerServiceDefinition.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}