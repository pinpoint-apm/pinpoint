/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.thrift;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ThriftTransportConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.rpc.client.ConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinpointClientFactoryProvider extends AbstractClientFactoryProvider implements Provider<PinpointClientFactory> {

    private final ThriftTransportConfig thriftTransportConfig;
    private final Provider<AgentInformation> agentInformation;
    private final Provider<ConnectionFactoryProvider> connectionFactoryProvider;
    private final CommandDispatcher commandDispatcher;

    @Inject
    public PinpointClientFactoryProvider(ThriftTransportConfig thriftTransportConfig, Provider<AgentInformation> agentInformation, CommandDispatcher commandDispatcher, Provider<ConnectionFactoryProvider> connectionFactoryProvider) {
        this.thriftTransportConfig = Assert.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        this.agentInformation = Assert.requireNonNull(agentInformation, "agentInformation");
        this.commandDispatcher = Assert.requireNonNull(commandDispatcher, "commandDispatcher");
        this.connectionFactoryProvider = Assert.requireNonNull(connectionFactoryProvider, "connectionFactoryProvider");

    }

    public PinpointClientFactory get() {
        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory(connectionFactoryProvider.get());
        pinpointClientFactory.setWriteTimeoutMillis(thriftTransportConfig.getTcpDataSenderPinpointClientWriteTimeout());
        pinpointClientFactory.setRequestTimeoutMillis(thriftTransportConfig.getTcpDataSenderPinpointClientRequestTimeout());
        pinpointClientFactory.setReconnectDelay(thriftTransportConfig.getTcpDataSenderPinpointClientReconnectInterval());
        pinpointClientFactory.setPingDelay(thriftTransportConfig.getTcpDataSenderPinpointClientPingInterval());
        pinpointClientFactory.setEnableWorkerPacketDelay(thriftTransportConfig.getTcpDataSenderPinpointClientHandshakeInterval());

        int writeBufferHighWaterMark = getByteSize(thriftTransportConfig.getTcpDataSenderPinpointClientWriteBufferHighWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(32));
        int writeBufferLowWaterMark = getByteSize(thriftTransportConfig.getTcpDataSenderPinpointClientWriteBufferLowWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(16));
        if (writeBufferLowWaterMark > writeBufferHighWaterMark) {
            logger.warn("must be writeBufferHighWaterMark({}) >= writeBufferLowWaterMark({})", writeBufferHighWaterMark, writeBufferLowWaterMark);
            writeBufferLowWaterMark = writeBufferHighWaterMark;
        }
        pinpointClientFactory.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        pinpointClientFactory.setWriteBufferLowWaterMark(writeBufferLowWaterMark);

        AgentInformation agentInformation = this.agentInformation.get();
        Map<String, Object> properties = toMap(agentInformation);

        boolean isSupportServerMode = thriftTransportConfig.isTcpDataSenderCommandAcceptEnable();

        if (isSupportServerMode) {
            pinpointClientFactory.setMessageListener(commandDispatcher);
            pinpointClientFactory.setServerStreamChannelMessageHandler(commandDispatcher);

            properties.put(HandshakePropertyType.SUPPORT_SERVER.getName(), true);
            properties.put(HandshakePropertyType.SUPPORT_COMMAND_LIST.getName(), commandDispatcher.getRegisteredCommandServiceCodes());
        } else {
            properties.put(HandshakePropertyType.SUPPORT_SERVER.getName(), false);
        }

        pinpointClientFactory.setProperties(properties);
        return pinpointClientFactory;
    }

    private Map<String, Object> toMap(AgentInformation agentInformation) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(HandshakePropertyType.AGENT_ID.getName(), agentInformation.getAgentId());
        map.put(HandshakePropertyType.APPLICATION_NAME.getName(), agentInformation.getApplicationName());
        map.put(HandshakePropertyType.HOSTNAME.getName(), agentInformation.getMachineName());
        map.put(HandshakePropertyType.IP.getName(), agentInformation.getHostIp());
        map.put(HandshakePropertyType.PID.getName(), agentInformation.getPid());
        map.put(HandshakePropertyType.SERVICE_TYPE.getName(), agentInformation.getServerType().getCode());
        map.put(HandshakePropertyType.START_TIMESTAMP.getName(), agentInformation.getStartTime());
        map.put(HandshakePropertyType.VERSION.getName(), agentInformation.getAgentVersion());

        return map;
    }
}
