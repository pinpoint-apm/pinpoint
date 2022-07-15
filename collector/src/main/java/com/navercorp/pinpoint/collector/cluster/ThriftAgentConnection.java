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

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;
import org.apache.thrift.TBase;

import java.util.List;
import java.util.Objects;

/**
 * @author koo.taejin
 */
public class ThriftAgentConnection implements ClusterPoint<byte[]> {

    private final PinpointServer pinpointServer;

    private final ClusterKey clusterKey;
    private final TCommandTypeVersion commandTypeVersion;
    private final List<TCommandType> supportCommandList;

    public static ClusterPoint<byte[]> newClusterPoint(PinpointServer pinpointServer, ChannelProperties channelProperties) {
        ClusterKey agentInfo = newClusterKey(channelProperties);
        TCommandTypeVersion commandTypeVersion = TCommandTypeVersion.getVersion(channelProperties.getAgentVersion());
        List<TCommandType> supportCommandList = SupportedCommandUtils.newSupportCommandList(channelProperties.getSupportCommand());
        return new ThriftAgentConnection(pinpointServer, agentInfo, commandTypeVersion, supportCommandList);
    }

    public ThriftAgentConnection(PinpointServer pinpointServer, ClusterKey clusterKey, TCommandTypeVersion commandTypeVersion, List<TCommandType> supportCommandList) {
        this.pinpointServer = Objects.requireNonNull(pinpointServer, "pinpointServer");
        this.clusterKey = Objects.requireNonNull(clusterKey, "clusterKey");
        this.commandTypeVersion = Objects.requireNonNull(commandTypeVersion, "commandTypeVersion");
        this.supportCommandList = Objects.requireNonNull(supportCommandList, "supportCommandList");
    }

    private static ClusterKey newClusterKey(ChannelProperties channelProperties) {
        String applicationName = channelProperties.getApplicationName();
        String agentId = channelProperties.getAgentId();
        long startTimeStamp = channelProperties.getStartTime();
        return new ClusterKey(applicationName, agentId, startTimeStamp);
    }

    @Override
    public Future<ResponseMessage> request(byte[] payload) {
        return pinpointServer.request(payload);
    }

    @Override
    public ClusterKey getDestClusterKey() {
        return clusterKey;
    }

    @Override
    public boolean isSupportCommand(TBase<?, ?> command) {
        for (TCommandType supportCommand : supportCommandList) {
            if (supportCommand.getClazz() == command.getClass()) {
                return true;
            }
        }

        if (commandTypeVersion.isSupportCommand(command)) {
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
        log.append(clusterKey);
        log.append(", supportCommandList:");
        log.append(supportCommandList);
        log.append(", pinpointServer:");
        log.append(pinpointServer);
        log.append(")");
        
        return log.toString();
    }

    @Override
    public int hashCode() {
        return clusterKey.hashCode();
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
