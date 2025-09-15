package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroup;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;

import java.io.IOException;
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

    public static class ServerGroupListSerializer extends JsonSerializer<ServerGroupListView> {

        public ServerGroupListSerializer() {
        }

        @Override
        public void serialize(ServerGroupListView view, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            ServerGroupList serverGroupList = view.getServerGroupList();
            jgen.writeStartObject();

            for (ServerGroup serverGroup : serverGroupList.getServerGroupList()) {
                jgen.writeFieldName(serverGroup.getHostName());
                jgen.writeStartObject();

                jgen.writeStringField("name", serverGroup.getHostName());
                jgen.writeStringField("status", null);
                HyperLinkFactory hyperLinkFactory = view.getHyperLinkFactory();
                List<HyperLink> hyperLinks = newHyperLink(hyperLinkFactory, serverGroup.getInstanceList());
                jgen.writeObjectField("linkList", hyperLinks);


                List<ServerInstance> serverInstances = serverGroup.getInstanceList();
                jgen.writeFieldName("instanceList");
                writeInstanceList(jgen, serverInstances);

                jgen.writeEndObject();
            }


            jgen.writeEndObject();

        }

        private void writeInstanceList(JsonGenerator jgen, List<ServerInstance> serverList) throws IOException {
            jgen.writeStartObject();
            for (ServerInstance serverInstance : serverList) {
                jgen.writeFieldName(serverInstance.getName());
                jgen.writeObject(new ServerInstanceView(serverInstance));
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
