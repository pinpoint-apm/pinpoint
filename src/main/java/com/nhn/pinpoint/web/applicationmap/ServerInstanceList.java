package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogramList;

import java.util.*;

/**
 * @author emeroad
 */
public class ServerInstanceList {

    private final Map<String, List<ServerInstance>> serverInstanceList = new TreeMap<String, List<ServerInstance>>();

    public Map<String, List<ServerInstance>> getServerInstanceList() {
        // list의 소트가 안되 있는 문제가 있음.
        return serverInstanceList;
    }

    /**
     * 어플리케이션에 속한 물리서버와 서버 인스턴스 정보를 채운다.
     *
     * @param hostHistogram
     */
    public void fillServerInstanceList(final CallHistogramList hostHistogram) {
        if (hostHistogram == null) {
            return;
        }

        for (CallHistogram callHistogram : hostHistogram.getCallHistogramList()) {
            final String instanceName = callHistogram.getId();
            final String hostName = getHostName(callHistogram.getId());
            final ServiceType serviceType = callHistogram.getServiceType();

            final ServerInstance serverInstance = new ServerInstance(instanceName, serviceType);
            List<ServerInstance> find = getServerInstanceList(hostName);
            addServerInstance(find, serverInstance);
        }
    }

    private void addServerInstance(List<ServerInstance> nodeList, ServerInstance serverInstance) {
        final String serverId = serverInstance.getId();
        for (ServerInstance  node : nodeList) {
            boolean equalsNode = node.getId().equals(serverId);
            if (equalsNode) {
                return;
            }
        }
        nodeList.add(serverInstance);
    }

    private String getHostName(String instanceName) {
        final int pos = instanceName.indexOf(':');
        if (pos > 0) {
            return instanceName.substring(0, pos);
        } else {
            return instanceName;
        }
    }

    public void fillServerInstanceList(final Set<AgentInfoBo> agentSet) {
        if (agentSet == null) {
            return;
        }
        for (AgentInfoBo agent : agentSet) {
            final String hostName = agent.getHostname();
            final ServerInstance serverInstance = new ServerInstance(agent);

            List<ServerInstance> find = getServerInstanceList(hostName);

            addServerInstance(find, serverInstance);
        }
    }

    private List<ServerInstance> getServerInstanceList(String hostName) {
        List<ServerInstance> find = serverInstanceList.get(hostName);
        if (find == null) {
            find = new ArrayList<ServerInstance>();
            serverInstanceList.put(hostName, find);
        }
        return find;
    }

}
