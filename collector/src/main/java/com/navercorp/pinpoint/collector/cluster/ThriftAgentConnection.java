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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.DefaultChannelProperties;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;

import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class ThriftAgentConnection implements ClusterPoint<byte[]> {

    private final PinpointServer pinpointServer;

    private final AgentInfo agentInfo;

    private final List<TCommandType> supportCommandList;

    public ThriftAgentConnection(PinpointServer pinpointServer) {
        Assert.requireNonNull(pinpointServer, "pinpointServer must not be null.");
        this.pinpointServer = pinpointServer;

        Map<Object, Object> properties = pinpointServer.getChannelProperties();
        ChannelProperties channelProperties = DefaultChannelProperties.newChannelProperties(properties);

        String applicationName = channelProperties.getApplicationName();

        String agentId = channelProperties.getAgentId();

        long startTimeStamp = channelProperties.getStartTime();

        String version = channelProperties.getAgentVersion();

        this.agentInfo = new AgentInfo(applicationName, agentId, startTimeStamp, version);
        this.supportCommandList = newSupportCommandList(channelProperties.getSupportCommand());
    }

    private List<TCommandType> newSupportCommandList(List<Integer> supportCommandList) {
        if (CollectionUtils.isEmpty(supportCommandList)) {
            return Collections.emptyList();
        }

        final List<TCommandType> result = new ArrayList<>();
        for (Integer supportCommandCode : supportCommandList) {
            TCommandType commandType = TCommandType.getType(supportCommandCode.shortValue());
            if (commandType != null) {
                result.add(commandType);
            }
        }
        return result;

    }

    @Override
    public Future request(byte[] payload) {
        return pinpointServer.request(payload);
    }

    @Override
    public AgentInfo getDestAgentInfo() {
        return agentInfo;
    }

    @Override
    public boolean isSupportCommand(TBase command) {
        for (TCommandType supportCommand : supportCommandList) {
            if (supportCommand.getClazz() == command.getClass()) {
                return true;
            }
        }

        TCommandTypeVersion commandVersion = TCommandTypeVersion.getVersion(agentInfo.getVersion());
        if (commandVersion.isSupportCommand(command)) {
            return true;
        }

        return false;
    }

    public PinpointServer getPinpointServer() {
        return pinpointServer;
    }
    
    @Override
    public String toString() {
        StringBuilder log = new StringBuilder(32);
        log.append(this.getClass().getSimpleName());
        log.append("(");
        log.append(agentInfo.toString());
        log.append(", supportCommandList:");
        log.append(supportCommandList);
        log.append(", pinpointServer:");
        log.append(pinpointServer);
        log.append(")");
        
        return log.toString();
    }

    @Override
    public int hashCode() {
        return agentInfo.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ThriftAgentConnection)) {
            return false;
        }

        if (this.getPinpointServer() == ((ThriftAgentConnection) obj).getPinpointServer()) {
            return true;
        }

        return false;
    }

}
