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

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * @author koo.taejin
 */
public final class CollectorUtils {

    private CollectorUtils() {
    }

    public static String getServerIdentifier() {
        String hostName = getHostName();
        long pid = ProcessHandle.current().pid();
        return pid + "@" + hostName;
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    public static String getHumanFriendlyServerIdentifier() {
        String hostName = getHostName();
        long pid = ProcessHandle.current().pid();
        return hostName + "@" + pid;
    }

    public static void checkAgentId(final AgentId agentId) {
        checkAgentId(AgentId.unwrap(agentId));
    }

    public static void checkAgentId(final String agentId) {
        if (!IdValidateUtils.validateId(agentId)) {
            throw new IllegalArgumentException("invalid agentId. agentId=" + agentId);
        }
    }

    public static void checkApplicationName(final String applicationName) {
        if (!IdValidateUtils.validateId(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN)) {
            throw new IllegalArgumentException("invalid applicationName. applicationName=" + applicationName);
        }
    }

    public static void checkAgentName(final String agentName) {
        if (StringUtils.isEmpty(agentName)) {
            return;
        }
        if (!IdValidateUtils.validateId(agentName, PinpointConstants.AGENT_NAME_MAX_LEN)) {
            throw new IllegalArgumentException("invalid agentName. agentName=" + agentName);
        }
    }
}
