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
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DispatchHandlerFactoryBean implements FactoryBean<DispatchHandler> {

    @Autowired
    private AcceptedTimeService acceptedTimeService;
    private final DispatchHandler delegate;

    private final HandlerManager handlerManager;

    public DispatchHandlerFactoryBean(DispatchHandler delegate, HandlerManager handlerManager) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager must not be null");
    }




    @Override
    public DispatchHandler getObject() throws Exception {
        return new DelegateDispatchHandler(acceptedTimeService, delegate, handlerManager);
    }

    @Override
    public Class<DispatchHandler> getObjectType() {
        return DispatchHandler.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
