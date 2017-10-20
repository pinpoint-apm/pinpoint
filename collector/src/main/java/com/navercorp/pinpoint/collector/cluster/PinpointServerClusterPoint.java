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
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TBase;
import org.springframework.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class PinpointServerClusterPoint implements TargetClusterPoint {

    private final PinpointServer pinpointServer;

    private final String applicationName;
    private final String agentId;
    private final long startTimeStamp;

    private final String version;
    private final List<TCommandType> supportCommandList;

    public PinpointServerClusterPoint(PinpointServer pinpointServer) {
        Assert.requireNonNull(pinpointServer, "pinpointServer must not be null.");
        this.pinpointServer = pinpointServer;

        Map<Object, Object> properties = pinpointServer.getChannelProperties();
        this.version = MapUtils.getString(properties, HandshakePropertyType.VERSION.getName());
        Assert.isTrue(!StringUtils.isBlank(version), "Version must not be null or empty.");

        this.supportCommandList = newSupportCommandList(properties);

        this.applicationName = MapUtils.getString(properties, HandshakePropertyType.APPLICATION_NAME.getName());
        Assert.isTrue(!StringUtils.isBlank(applicationName), "ApplicationName must not be null or empty.");

        this.agentId = MapUtils.getString(properties, HandshakePropertyType.AGENT_ID.getName());
        Assert.isTrue(!StringUtils.isBlank(agentId), "AgentId must not be null or empty.");

        this.startTimeStamp = MapUtils.getLong(properties, HandshakePropertyType.START_TIMESTAMP.getName());
        Assert.isTrue(startTimeStamp > 0, "StartTimeStamp is must greater than zero.");
    }

    private List<TCommandType> newSupportCommandList(Map<Object, Object> properties) {
        final Object supportCommandCodeList = properties.get(HandshakePropertyType.SUPPORT_COMMAND_LIST.getName());
        if (!(supportCommandCodeList instanceof List)) {
            return Collections.emptyList();
        }

        final List<TCommandType> result = new ArrayList<>();
        for (Object supportCommandCode : (List)supportCommandCodeList) {
            if (supportCommandCode instanceof Number) {
                TCommandType commandType = TCommandType.getType(NumberUtils.convertNumberToTargetClass((Number) supportCommandCode, Short.class));
                if (commandType != null) {
                    result.add(commandType);
                }
            }
        }
        return result;

    }

    @Override
    public void send(byte[] payload) {
        pinpointServer.send(payload);
    }

    @Override
    public Future request(byte[] payload) {
        return pinpointServer.request(payload);
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    @Override
    public String gerVersion() {
        return version;
    }

    @Override
    public boolean isSupportCommand(TBase command) {
        for (TCommandType supportCommand : supportCommandList) {
            if (supportCommand.getClazz() == command.getClass()) {
                return true;
            }
        }

        TCommandTypeVersion commandVersion = TCommandTypeVersion.getVersion(version);
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
        log.append(applicationName);
        log.append("/");
        log.append(agentId);
        log.append("/");
        log.append(startTimeStamp);
        log.append(")");
        log.append(", version:");
        log.append(version);
        log.append(", supportCommandList:");
        log.append(supportCommandList);
        log.append(", pinpointServer:");
        log.append(pinpointServer);
        
        return log.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        
        result = prime * result + ((applicationName == null) ? 0 : applicationName.hashCode());
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        result = prime * result + (int) (startTimeStamp ^ (startTimeStamp >>> 32));
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PinpointServerClusterPoint)) {
            return false;
        }

        if (this.getPinpointServer() == ((PinpointServerClusterPoint) obj).getPinpointServer()) {
            return true;
        }

        return false;
    }

}
