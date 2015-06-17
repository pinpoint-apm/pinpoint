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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class PinpointServerClusterPoint implements TargetClusterPoint {

    private final PinpointServer pinpointServer;

    private final String applicationName;
    private final String agentId;
    private final long startTimeStamp;

    private final String version;

    public PinpointServerClusterPoint(PinpointServer pinpointServer) {
        AssertUtils.assertNotNull(pinpointServer, "pinpointServer may not be null.");
        this.pinpointServer = pinpointServer;

        Map<Object, Object> properties = pinpointServer.getChannelProperties();
        this.version = MapUtils.getString(properties, AgentHandshakePropertyType.VERSION.getName());
        AssertUtils.assertTrue(!StringUtils.isBlank(version), "Version may not be null or empty.");

        this.applicationName = MapUtils.getString(properties, AgentHandshakePropertyType.APPLICATION_NAME.getName());
        AssertUtils.assertTrue(!StringUtils.isBlank(applicationName), "ApplicationName may not be null or empty.");

        this.agentId = MapUtils.getString(properties, AgentHandshakePropertyType.AGENT_ID.getName());
        AssertUtils.assertTrue(!StringUtils.isBlank(agentId), "AgentId may not be null or empty.");

        this.startTimeStamp = MapUtils.getLong(properties, AgentHandshakePropertyType.START_TIMESTAMP.getName());
        AssertUtils.assertTrue(startTimeStamp > 0, "StartTimeStamp is must greater than zero.");
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
