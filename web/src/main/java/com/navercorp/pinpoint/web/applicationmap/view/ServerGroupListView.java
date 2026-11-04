package com.navercorp.pinpoint.web.applicationmap.view;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroup;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;

import java.util.List;
import java.util.Objects;

@JsonSerialize(using = ServerGroupListView.ServerGroupListSerializer.class)
public class ServerGroupListView {
    private final ServerGroupList serverGroupList;
    private final HyperLinkFactory hyperLinkFactory;

    public ServerGroupListView(ServerGroupList serverGroupList, HyperLinkFactory hyperLinkFactory) {
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
        this.hyperLinkFactory = hyperLinkFactory;
    }

    public ServerGroupList getServerGroupList() {
        return serverGroupList;
    }

    public HyperLinkFactory getHyperLinkFactory() {
        return hyperLinkFactory;
    }

    public static class ServerGroupListSerializer extends ValueSerializer<ServerGroupListView> {

        public ServerGroupListSerializer() {
        }

        @Override
        public void serialize(ServerGroupListView view, JsonGenerator jgen, SerializationContext provider) {
            ServerGroupList serverGroupList = view.getServerGroupList();
            jgen.writeStartObject();

            for (ServerGroup serverGroup : serverGroupList.getServerGroupList()) {
                jgen.writeName(serverGroup.getHostName());
                jgen.writeStartObject();

                jgen.writeStringProperty("name", serverGroup.getHostName());
                jgen.writeStringProperty("status", null);
                HyperLinkFactory hyperLinkFactory = view.getHyperLinkFactory();
                List<HyperLink> hyperLinks = newHyperLink(hyperLinkFactory, serverGroup.getInstanceList());
                jgen.writePOJOProperty("linkList", hyperLinks);


                List<ServerInstance> serverInstances = serverGroup.getInstanceList();
                jgen.writeName("instanceList");
                writeInstanceList(jgen, serverInstances);

                jgen.writeEndObject();
            }


            jgen.writeEndObject();

        }

        private void writeInstanceList(JsonGenerator jgen, List<ServerInstance> serverList) {
            jgen.writeStartObject();
            for (ServerInstance serverInstance : serverList) {
                jgen.writeName(serverInstance.getName());
                jgen.writePOJO(new ServerInstanceView(serverInstance));
            }

            jgen.writeEndObject();
        }


        private List<HyperLink> newHyperLink(HyperLinkFactory hyperLinkFactory, List<ServerInstance> serverList) {
            if (serverList.isEmpty()) {
                return List.of();
            }
            ServerInstance first = serverList.get(0);
            return hyperLinkFactory.build(LinkSources.from(first.getHostName(), first.getIp(), first.getServiceType()));
        }

    }
}
