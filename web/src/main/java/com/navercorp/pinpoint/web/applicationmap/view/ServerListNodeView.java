/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.navercorp.pinpoint.common.server.util.json.JacksonWriterUtils;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;

import java.io.IOException;

public interface ServerListNodeView {

    default void writeServerList(NodeView nodeView, JsonGenerator jgen) throws IOException {
    }

    static ServerListNodeView detailedView() {
        return new DetailedServerListNodeView();
    }

    static ServerListNodeView emptyView() {
        return new ServerListNodeView() {
        };
    }


    class DetailedServerListNodeView implements ServerListNodeView {

        @Override
        public void writeServerList(NodeView nodeView, JsonGenerator jgen) throws IOException {
            Node node = nodeView.getNode();
            if (node.getServiceType().isUnknown()) {
                JacksonWriterUtils.writeEmptyObject(jgen, "serverList");
            } else {
                ServerGroupList serverGroupList = node.getServerGroupList();
                jgen.writeObjectField("serverList", new ServerGroupListView(serverGroupList, nodeView.getHyperLinkFactory()));
            }
        }
    }

}
