/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.metadata.AgentInfo;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;
import com.navercorp.pinpoint.thrift.dto.TJvmInfo;
import com.navercorp.pinpoint.thrift.dto.TServerMetaData;
import com.navercorp.pinpoint.thrift.dto.TServiceInfo;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MetadataMessageConverter implements MessageConverter<TBase<?, ?>> {

    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;
    private final JvmGcTypeThriftMessageConverter jvmGcTypeMessageConverter = new JvmGcTypeThriftMessageConverter();

    public MetadataMessageConverter(String applicationName, String agentId, long agentStartTime) {
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName");
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
    }

    @Override
    public TBase<?, ?> toMessage(Object message) {
        if (message instanceof AgentInfo) {
            final AgentInfo agentInfo = (AgentInfo) message;
            return convertAgentInfo(agentInfo);
        } else if (message instanceof SqlMetaData) {
            final SqlMetaData sqlMetaData = (SqlMetaData) message;
            return convertSqlMetaData(sqlMetaData);
        } else if (message instanceof ApiMetaData) {
            final ApiMetaData apiMetaData = (ApiMetaData) message;
            return convertApiMetaData(apiMetaData);
        } else if (message instanceof StringMetaData) {
            final StringMetaData stringMetaData = (StringMetaData) message;
            return convertStringMetaData(stringMetaData);
        }
        return null;
    }

    public TAgentInfo convertAgentInfo(final AgentInfo agentInfo) {
        final AgentInformation agentInformation = agentInfo.getAgentInformation();

        final TAgentInfo tAgentInfo = new TAgentInfo();
        tAgentInfo.setIp(agentInformation.getHostIp());
        tAgentInfo.setHostname(agentInformation.getMachineName());
        tAgentInfo.setPorts("");
        tAgentInfo.setAgentId(agentInformation.getAgentId());
        tAgentInfo.setApplicationName(agentInformation.getApplicationName());
        tAgentInfo.setContainer(agentInformation.isContainer());
        tAgentInfo.setPid(agentInformation.getPid());
        tAgentInfo.setStartTimestamp(agentInformation.getStartTime());
        tAgentInfo.setServiceType(agentInformation.getServerType().getCode());
        tAgentInfo.setVmVersion(agentInformation.getJvmVersion());
        tAgentInfo.setAgentVersion(Version.VERSION);

        final TServerMetaData tServerMetaData = convertServerMetaData(agentInfo.getServerMetaData());
        tAgentInfo.setServerMetaData(tServerMetaData);

        final TJvmInfo tJvmInfo = convertJvmInfo(agentInfo.getJvmInfo());
        tAgentInfo.setJvmInfo(tJvmInfo);
        return tAgentInfo;
    }

    private TServerMetaData convertServerMetaData(final ServerMetaData serverMetaData) {
        if (serverMetaData == null) {
            return null;
        }

        final TServerMetaData tServerMetaData = new TServerMetaData();
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

    private TJvmInfo convertJvmInfo(final JvmInformation jvmInformation) {
        final TJvmInfo tJvmInfo = new TJvmInfo();
        tJvmInfo.setVmVersion(jvmInformation.getJvmVersion());
        TJvmGcType gcType = this.jvmGcTypeMessageConverter.toMessage(jvmInformation.getJvmGcType());
        tJvmInfo.setGcType(gcType);
        return tJvmInfo;
    }

    private TSqlMetaData convertSqlMetaData(final SqlMetaData sqlMetaData) {
        return new TSqlMetaData(agentId, agentStartTime, sqlMetaData.getSqlId(), sqlMetaData.getSql());
    }

    private TApiMetaData convertApiMetaData(final ApiMetaData apiMetaData) {
        final TApiMetaData tApiMetaData = new TApiMetaData(agentId, agentStartTime, apiMetaData.getApiId(), apiMetaData.getApiInfo());
        tApiMetaData.setLine(apiMetaData.getLine());
        tApiMetaData.setType(apiMetaData.getType());
        return tApiMetaData;
    }

    private TStringMetaData convertStringMetaData(final StringMetaData stringMetaData) {
        return new TStringMetaData(agentId, agentStartTime, stringMetaData.getStringId(), stringMetaData.getStringValue());
    }
}