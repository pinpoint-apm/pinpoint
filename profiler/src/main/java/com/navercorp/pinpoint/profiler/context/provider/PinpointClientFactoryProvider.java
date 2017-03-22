/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinpointClientFactoryProvider implements Provider<PinpointClientFactory> {

    private final ProfilerConfig profilerConfig;
    private final Provider<AgentInformation> agentInformation;
    private final CommandDispatcher commandDispatcher;

    @Inject
    public PinpointClientFactoryProvider(ProfilerConfig profilerConfig, Provider<AgentInformation> agentInformation, CommandDispatcher commandDispatcher) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        if (commandDispatcher == null) {
            throw new NullPointerException("commandDispatcher must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.agentInformation = agentInformation;
        this.commandDispatcher = commandDispatcher;
    }

    public PinpointClientFactory get() {
        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory();
        pinpointClientFactory.setTimeoutMillis(1000 * 5);

        AgentInformation agentInformation = this.agentInformation.get();
        Map<String, Object> properties = toMap(agentInformation);

        boolean isSupportServerMode = profilerConfig.isTcpDataSenderCommandAcceptEnable();

        if (isSupportServerMode) {
            pinpointClientFactory.setMessageListener(commandDispatcher);
            pinpointClientFactory.setServerStreamChannelMessageListener(commandDispatcher);

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
