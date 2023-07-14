package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.view.histogram.AgentHistogramView;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Objects;

public class NodeAgentHistogramList {
    private final String nodeName;
    private final List<AgentHistogramView> agentHistogramList;
    private final ServerGroupList serverGroupList;

    public NodeAgentHistogramList(String nodeName, NodeHistogram nodeHistogram, ServerGroupList serverGroupList) {
        this.nodeName = Objects.requireNonNull(nodeName, "nodeName");
        Objects.requireNonNull(nodeHistogram, "nodeHistogram");
        this.agentHistogramList = nodeHistogram.getAgentHistogramViewList();
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
    }

    public NodeAgentHistogramList(Application application, NodeHistogram nodeHistogram, ServerGroupList serverGroupList) {
        this(NodeName.of(application).getName(), nodeHistogram, serverGroupList);
    }

    @JsonProperty("key")
    public String getNodeName() {
        return nodeName;
    }

    @JsonProperty("agentHistogramList")
    public List<AgentHistogramView> getAgentHistogramList() {
        return agentHistogramList;
    }

    @JsonProperty("serverList")
    public ServerGroupList getServerList() {
        return serverGroupList;
    }

}
