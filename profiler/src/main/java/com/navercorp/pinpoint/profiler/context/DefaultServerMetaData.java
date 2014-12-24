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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;

/**
 * @author hyungil.jeong
 */
public class DefaultServerMetaData implements ServerMetaData {
    
    private final String serverInfo;
    private final List<String> vmArgs;
    private final Map<Integer, String> connectors;
    private final List<ServiceInfo> serviceInfo;

    public DefaultServerMetaData(String serverInfo, List<String> vmArgs, Map<Integer, String> connectors, List<ServiceInfo> serviceInfo) {
        this.serverInfo = serverInfo;
        this.vmArgs = vmArgs;
        this.connectors = connectors;
        this.serviceInfo = serviceInfo;
    }
    
    @Override
    public String getServerInfo() {
        return this.serverInfo;
    }

    @Override
    public List<String> getVmArgs() {
        return Collections.unmodifiableList(this.vmArgs);
    }

    @Override
    public Map<Integer, String> getConnectors() {
        return Collections.unmodifiableMap(this.connectors);
    }

    @Override
    public List<ServiceInfo> getServiceInfos() {
        return Collections.unmodifiableList(this.serviceInfo);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultServerMetaData{");
        sb.append("serverInfo='").append(serverInfo).append('\'');
        sb.append(", vmArgs=").append(vmArgs);
        sb.append(", connectors=").append(connectors);
        sb.append(", serviceInfo=").append(serviceInfo).append('}');
        return sb.toString();
    }

}
