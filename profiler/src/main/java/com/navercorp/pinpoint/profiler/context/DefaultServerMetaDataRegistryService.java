/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author HyunGil Jeong
 */
public class DefaultServerMetaDataRegistryService implements ServerMetaDataRegistryService {

    private final List<OnChangeListener> listeners = new CopyOnWriteArrayList<OnChangeListener>();

    private volatile String serverName;
    private final List<String> vmArgs;
    private final Map<Integer, String> connectors = new ConcurrentHashMap<Integer, String>();
    private final Queue<ServiceInfo> serviceInfos = new ConcurrentLinkedQueue<ServiceInfo>();

    public DefaultServerMetaDataRegistryService(List<String> vmArgs) {
        this.vmArgs = Collections.unmodifiableList(vmArgs);
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void addConnector(String protocol, int port) {
        this.connectors.put(port, protocol);
    }

    @Override
    public void addServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfos.add(serviceInfo);
    }

    @Override
    public ServerMetaData getServerMetaData() {
        return createServerMetaData();
    }

    @Override
    public boolean addListener(OnChangeListener listener) {
        if (listener == null) {
            return false;
        }
        return this.listeners.add(listener);
    }

    @Override
    public boolean removeListener(OnChangeListener listener) {
        if (listener == null) {
            return false;
        }
        return this.listeners.remove(listener);
    }

    @Override
    public void notifyListeners() {
        for (OnChangeListener listener : this.listeners) {
            listener.onServerMetaDataChange();
        }
    }

    private ServerMetaData createServerMetaData() {
        String serverName = this.serverName == null ? "" : this.serverName;
        List<String> vmArgs =
                this.vmArgs == null ? Collections.<String>emptyList() : new ArrayList<String>(this.vmArgs);
        Map<Integer, String> connectors =
                this.connectors.isEmpty() ? Collections.<Integer, String>emptyMap() : new HashMap<Integer, String>(this.connectors);
        List<ServiceInfo> serviceInfos =
                this.serviceInfos.isEmpty() ? Collections.<ServiceInfo>emptyList() : new ArrayList<ServiceInfo>(this.serviceInfos);
        return new DefaultServerMetaData(serverName, vmArgs, connectors, serviceInfos);
    }
}
