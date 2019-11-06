/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;

/**
 * @author hyungil.jeong
 */
public class DefaultServerMetaDataHolder implements ServerMetaDataHolder {
    
    private final List<ServerMetaDataListener> listeners = new CopyOnWriteArrayList<ServerMetaDataListener>();

    private final ServerMetaDataRegistryService serverMetaDataRegistryService;

    public DefaultServerMetaDataHolder(ServerMetaDataRegistryService serverMetaDataRegistryService) {
        if (serverMetaDataRegistryService == null) {
            throw new NullPointerException("serverMetaDataRegistryService");
        }
        this.serverMetaDataRegistryService = serverMetaDataRegistryService;
    }

    @Override
    public void setServerName(String serverName) {
        this.serverMetaDataRegistryService.setServerName(serverName);
    }

    @Override
    public void addConnector(String protocol, int port) {
        this.serverMetaDataRegistryService.addConnector(protocol, port);
    }
    
    @Override
    public void addServiceInfo(String serviceName, List<String> serviceLibs) {
        ServiceInfo serviceInfo = new DefaultServiceInfo(serviceName, serviceLibs);
        this.serverMetaDataRegistryService.addServiceInfo(serviceInfo);
    }

    @Override
    public void addListener(ServerMetaDataListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(ServerMetaDataListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void notifyListeners() {
        this.serverMetaDataRegistryService.notifyListeners();
    }
}
