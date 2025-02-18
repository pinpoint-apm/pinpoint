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
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.DefaultAgentInformation;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;
import com.navercorp.pinpoint.profiler.context.module.ClusterNamespace;
import com.navercorp.pinpoint.profiler.context.module.Container;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentInformationProvider implements Provider<AgentInformation> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectName objectName;
    private final boolean isContainer;
    private final long agentStartTime;
    private final ServiceType serverType;

    private final Provider<String> clusterNamespaceProvider;

    @Inject
    public AgentInformationProvider(ObjectName objectName,
                                    @Container boolean isContainer,
                                    @AgentStartTime long agentStartTime,
                                    @ApplicationServerType ServiceType serverType,
                                    @ClusterNamespace Provider<String> clusterNamespaceProvider) {
        this.objectName = Objects.requireNonNull(objectName, "objectName");
        this.isContainer = isContainer;
        this.agentStartTime = agentStartTime;
        this.serverType = Objects.requireNonNull(serverType, "serverType");
        this.clusterNamespaceProvider = clusterNamespaceProvider;
    }

    public AgentInformation get() {
        return createAgentInformation();
    }

    public AgentInformation createAgentInformation() {

        final String machineName = NetworkUtils.getHostName();
        String hostIp = getHostIp();

        final int pid = RuntimeMXBeanUtils.getPid();
        final String jvmVersion = JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION);
        String clusterNamespace = this.clusterNamespaceProvider.get();
        return new DefaultAgentInformation(objectName, isContainer, agentStartTime, pid,
                machineName, hostIp, serverType, jvmVersion, Version.VERSION,
                clusterNamespace);
    }

    private String getHostIp() {
        String hostIp;
        try {
            hostIp = NetworkUtils.getHostIp();
        } catch (UnknownHostException e) {
            logger.info("Cannot resolve HostIp. HostIp is set to {}", NetworkUtils.LOOPBACK_ADDRESS_V4_1, e);
            hostIp = NetworkUtils.LOOPBACK_ADDRESS_V4_1;
        }
        return NetworkUtils.getRepresentationHostIp(hostIp);
    }
}
