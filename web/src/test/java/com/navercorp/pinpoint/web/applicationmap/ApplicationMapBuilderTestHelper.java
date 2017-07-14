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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author HyunGil Jeong
 */
public class ApplicationMapBuilderTestHelper {

    private static final ServiceType WAS_TYPE = ServiceType.TEST_STAND_ALONE;
    private static final ServiceType USER_TYPE = ServiceType.USER;
    private static final ServiceType UNKNOWN_TYPE = ServiceType.UNKNOWN;
    private static final ServiceType TERMINAL_TYPE = ServiceTypeFactory.of(2000, "TERMINAL", TERMINAL, INCLUDE_DESTINATION_ID);
    private static final ServiceType RPC_TYPE = ServiceTypeFactory.of(9000, "RPC", RECORD_STATISTICS);

    public static ApplicationMapBuilder createApplicationMapBuilder(Range range) {
        NodeHistogramAppenderFactory nodeHistogramAppenderFactory = new NodeHistogramAppenderFactory("serial", 16);
        ServerInfoAppenderFactory serverInfoAppenderFactory = new ServerInfoAppenderFactory("serial", 16);
        return new ApplicationMapBuilder(range, nodeHistogramAppenderFactory, serverInfoAppenderFactory);
    }

    public static ApplicationMapBuilder createApplicationMapBuilder_parallelAppenders(Range range) {
        NodeHistogramAppenderFactory nodeHistogramAppenderFactory = new NodeHistogramAppenderFactory("parallel", 16);
        ServerInfoAppenderFactory serverInfoAppenderFactory = new ServerInfoAppenderFactory("parallel", 16);
        return new ApplicationMapBuilder(range, nodeHistogramAppenderFactory, serverInfoAppenderFactory);

    }

    public static int getExpectedNumNodes(int calleeDepth, int callerDepth) {
        if (calleeDepth < 1) {
            throw new IllegalArgumentException("calleeDepth must be greater than 0");
        }
        if (callerDepth < 1) {
            throw new IllegalArgumentException("callerDepth must be greater than 0");
        }
        int numCenterNodes = 1;
        int numTerminalNodes = 1;
        return numCenterNodes + numTerminalNodes + calleeDepth + callerDepth;
    }

    public static int getExpectedNumLinks(int calleeDepth, int callerDepth) {
        int expectedNumNodes = getExpectedNumNodes(calleeDepth, callerDepth);
        return expectedNumNodes - 1;
    }

    public static LinkDataDuplexMap createLinkDataDuplexMap(int calleeDepth, int callerDepth) {
        if (calleeDepth < 1) {
            throw new IllegalArgumentException("calleeDepth must be greater than 0");
        }
        if (callerDepth < 1) {
            throw new IllegalArgumentException("callerDepth must be greater than 0");
        }
        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        for (int i = 0; i < calleeDepth - 1; ++i) {
            LinkData targetLinkData = createTargetLinkData(i);
            linkDataDuplexMap.addTargetLinkData(targetLinkData);
        }
        linkDataDuplexMap.addTargetLinkData(createUserTargetLinkData(calleeDepth - 1));

        linkDataDuplexMap.addSourceLinkData(createTerminalSourceLinkData(0));

        for (int i = 0; i < callerDepth - 1; ++i) {
            LinkData sourceLinkData = createSourceLinkData(i);
            linkDataDuplexMap.addSourceLinkData(sourceLinkData);
        }
        linkDataDuplexMap.addSourceLinkData(createUnknownSourceLinkData(callerDepth - 1));
        return linkDataDuplexMap;
    }

    public static LinkData createTargetLinkData(int depth) {
        int fromDepth = (depth + 1) * -1;
        int toDepth = depth * -1;
        Application fromApplication = createApplicationFromDepth(fromDepth);
        Application toApplication = createApplicationFromDepth(toDepth);
        LinkData targetLinkData = new LinkData(fromApplication, toApplication);
        targetLinkData.addLinkData(
                fromApplication.getName(), WAS_TYPE,
                createAgentIdFromDepth(toDepth), WAS_TYPE,
                System.currentTimeMillis(), WAS_TYPE.getHistogramSchema().getNormalSlot().getSlotTime(), 1);
        return targetLinkData;
    }

    public static LinkData createUserTargetLinkData(int depth) {
        int toDepth = depth * -1;
        Application toApplication = createApplicationFromDepth(toDepth);
        Application fromApplication = createUserApplication(toApplication);
        LinkData targetLinkData = new LinkData(fromApplication, toApplication);
        targetLinkData.addLinkData(
                fromApplication.getName(), USER_TYPE,
                createAgentIdFromDepth(toDepth), WAS_TYPE,
                System.currentTimeMillis(), WAS_TYPE.getHistogramSchema().getNormalSlot().getSlotTime(), 1);
        return targetLinkData;
    }

    public static LinkData createSourceLinkData(int depth) {
        int fromDepth = depth;
        int toDepth = depth + 1;
        Application fromApplication = createApplicationFromDepth(fromDepth);
        Application toApplication = createApplicationFromDepth(toDepth);
        LinkData sourceLinkData = new LinkData(fromApplication, toApplication);
        sourceLinkData.addLinkData(
                createAgentIdFromDepth(fromDepth), WAS_TYPE,
                createHostnameFromDepth(toDepth), RPC_TYPE,
                System.currentTimeMillis(), RPC_TYPE.getHistogramSchema().getNormalSlot().getSlotTime(), 1);
        return sourceLinkData;
    }

    public static LinkData createTerminalSourceLinkData(int depth) {
        int fromDepth = depth;
        Application fromApplication = createApplicationFromDepth(fromDepth);
        Application toApplication = createTerminalApplication(fromApplication);
        LinkData sourceLinkData = new LinkData(fromApplication, toApplication);
        sourceLinkData.addLinkData(
                createAgentIdFromDepth(fromDepth), WAS_TYPE,
                createIpFromDepth(fromDepth), TERMINAL_TYPE,
                System.currentTimeMillis(), TERMINAL_TYPE.getHistogramSchema().getNormalSlot().getSlotTime(), 1);
        return sourceLinkData;
    }

    public static LinkData createUnknownSourceLinkData(int depth) {
        int fromDepth = depth;
        int toDepth = depth + 1;
        Application fromApplication = createApplicationFromDepth(fromDepth);
        Application toApplication = createUnknownApplication(fromApplication);
        LinkData sourceLinkData = new LinkData(fromApplication, toApplication);
        sourceLinkData.addLinkData(
                createAgentIdFromDepth(fromDepth), WAS_TYPE,
                createHostnameFromDepth(toDepth), RPC_TYPE,
                System.currentTimeMillis(), RPC_TYPE.getHistogramSchema().getNormalSlot().getSlotTime(), 1);
        return sourceLinkData;
    }

    public static Application createApplicationFromDepth(int depth) {
        return new Application(createApplicationNameFromDepth(depth), WAS_TYPE);
    }

    public static Application createUserApplication(Application toApplication) {
        String userApplicationName = toApplication.getName() + "_" + toApplication.getServiceType().getName();
        return new Application(userApplicationName, USER_TYPE);
    }

    public static Application createTerminalApplication(Application fromApplication) {
        int depth = getDepthFromApplicationName(fromApplication.getName());
        String terminalApplicationName = "destinationId_" + depth;
        return new Application(terminalApplicationName, TERMINAL_TYPE);
    }

    public static Application createUnknownApplication(Application fromApplication) {
        int depth = getDepthFromApplicationName(fromApplication.getName());
        return new Application(createHostnameFromDepth(depth), UNKNOWN_TYPE);
    }

    public static String createApplicationNameFromDepth(int depth) {
        return "APP[" + depth + "]";
    }

    public static int getDepthFromApplicationName(String applicationName) {
        int startIndex = applicationName.indexOf("[");
        int endIndex = applicationName.indexOf("]");
        if (startIndex < 0 || endIndex < 0) {
            throw new IllegalArgumentException("Invalid applicationName : " + applicationName);
        }
        return Integer.parseInt(applicationName.substring(startIndex + 1, endIndex));
    }

    public static String createAgentIdFromDepth(int depth) {
        return "agent[" + depth + "]";
    }

    public static String createHostnameFromDepth(int depth) {
        return "www.host" + depth + ".com";
    }

    public static String createIpFromDepth(int depth) {
        return depth + "." + depth + "." + depth + "." + depth + ":" + depth;
    }

    public static AgentInfo createAgentInfoFromApplicationName(String applicationName) {
        int depth = getDepthFromApplicationName(applicationName);
        String agentId = createAgentIdFromDepth(depth);
        String hostName = createHostnameFromDepth(depth);
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName(applicationName);
        agentInfo.setAgentId(agentId);
        agentInfo.setHostName(hostName);
        return agentInfo;
    }
}
