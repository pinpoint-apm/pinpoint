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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;
import com.navercorp.pinpoint.thrift.dto.TJvmInfo;
import com.navercorp.pinpoint.thrift.dto.TServerMetaData;
import com.navercorp.pinpoint.thrift.dto.TServiceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoFactory {

    private final AgentInformation agentInformation;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;
    private final JvmInformation jvmInformation;

    public AgentInfoFactory(AgentInformation agentInformation, ServerMetaDataRegistryService serverMetaDataRegistryService, JvmInformation jvmInformation) {
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        if (serverMetaDataRegistryService == null) {
            throw new NullPointerException("serverMetaDataRegistryService must not be null");
        }
        if (jvmInformation == null) {
            throw new NullPointerException("jvmInformation must not be null");
        }
        this.agentInformation = agentInformation;
        this.serverMetaDataRegistryService = serverMetaDataRegistryService;
        this.jvmInformation = jvmInformation;
    }

    public TAgentInfo createAgentInfo() {
        final TAgentInfo tAgentInfo = new TAgentInfo();
        tAgentInfo.setIp(agentInformation.getHostIp());
        tAgentInfo.setHostname(agentInformation.getMachineName());
        tAgentInfo.setPorts("");
        tAgentInfo.setAgentId(agentInformation.getAgentId());
        tAgentInfo.setApplicationName(agentInformation.getApplicationName());
        tAgentInfo.setPid(agentInformation.getPid());
        tAgentInfo.setStartTimestamp(agentInformation.getStartTime());
        tAgentInfo.setServiceType(agentInformation.getServerType().getCode());
        tAgentInfo.setVmVersion(agentInformation.getJvmVersion());
        tAgentInfo.setAgentVersion(Version.VERSION);
        TServerMetaData tServerMetaData = createTServerMetaData();
        tAgentInfo.setServerMetaData(tServerMetaData);
        TJvmInfo tJvmInfo = createTJvmInfo();
        tAgentInfo.setJvmInfo(tJvmInfo);
        return tAgentInfo;
    }

    private TServerMetaData createTServerMetaData() {
        ServerMetaData serverMetaData = serverMetaDataRegistryService.getServerMetaData();
        if (serverMetaData == null) {
            return null;
        }
        TServerMetaData tServerMetaData = new TServerMetaData();
        tServerMetaData.setServerInfo(serverMetaData.getServerInfo());
        tServerMetaData.setVmArgs(serverMetaData.getVmArgs());
        List<TServiceInfo> tServiceInfos = new ArrayList<TServiceInfo>();
        for (ServiceInfo serviceInfo : serverMetaData.getServiceInfos()) {
            TServiceInfo tServiceInfo = new TServiceInfo();
            tServiceInfo.setServiceName(serviceInfo.getServiceName());
            tServiceInfo.setServiceLibs(serviceInfo.getServiceLibs());
            tServiceInfos.add(tServiceInfo);
        }
        tServerMetaData.setServiceInfos(tServiceInfos);
        return tServerMetaData;
    }

    private TJvmInfo createTJvmInfo() {
        TJvmInfo tJvmInfo = new TJvmInfo();
        tJvmInfo.setVmVersion(jvmInformation.getJvmVersion());
        TJvmGcType gcType = TJvmGcType.findByValue(jvmInformation.getGcTypeCode());
        if (gcType == null) {
            gcType = TJvmGcType.UNKNOWN;
        }
        tJvmInfo.setGcType(gcType);
        return tJvmInfo;
    }
}
