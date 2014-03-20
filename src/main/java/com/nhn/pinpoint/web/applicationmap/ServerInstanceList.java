package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.view.ServerInstanceListSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
@JsonSerialize(using=ServerInstanceListSerializer.class)
public class ServerInstanceList {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, List<ServerInstance>> serverInstanceList = new TreeMap<String, List<ServerInstance>>();

    public ServerInstanceList() {
    }

    public Map<String, List<ServerInstance>> getServerInstanceList() {
        // list의 소트가 안되 있는 문제가 있음.
        return serverInstanceList;
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



    private List<ServerInstance> getServerInstanceList(String hostName) {
        List<ServerInstance> find = serverInstanceList.get(hostName);
        if (find == null) {
            find = new ArrayList<ServerInstance>();
            serverInstanceList.put(hostName, find);
        }
        return find;
    }

    void addServerInstance(String hostName, ServerInstance serverInstance) {
        List<ServerInstance> find = getServerInstanceList(hostName);
        addServerInstance(find, serverInstance);
    }


}
