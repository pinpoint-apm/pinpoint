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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.FactoryBean;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DispatchHandlerFactoryBean<REQ, RES> implements FactoryBean<DispatchHandler<REQ, RES>> {

    private DispatchHandler<REQ, RES> dispatchHandler;

    private HandlerManager handlerManager;

    public DispatchHandlerFactoryBean() {

    }

    public void setDispatchHandler(DispatchHandler<REQ, RES> dispatchHandler) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
    }

    public void setHandlerManager(HandlerManager handlerManager) {
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager");
    }

    @Override
    public DispatchHandler<REQ, RES> getObject() throws Exception {
        return new DelegateDispatchHandler<>(dispatchHandler, handlerManager);
    }

    @Override
    public Class<DispatchHandler> getObjectType() {
        return DispatchHandler.class;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        Objects.requireNonNull(handlerManager, "handlerManager");
    }
}
