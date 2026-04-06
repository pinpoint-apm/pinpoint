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

package com.navercorp.pinpoint.web.applicationmap.servicemap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@JsonSerialize(using = ServiceGroupNodeView.ServiceGroupNodeViewSerializer.class)
public class ServiceGroupNodeView implements NodeViewEntry {
    private final String serviceName;
    private final List<NodeView> nodes;

    public ServiceGroupNodeView(String serviceName, List<NodeView> nodes) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.nodes = Objects.requireNonNull(nodes, "nodes");
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<NodeView> getNodes() {
        return nodes;
    }

    @JsonComponent
    public static class ServiceGroupNodeViewSerializer extends JsonSerializer<ServiceGroupNodeView> {

        @Override
        public void serialize(ServiceGroupNodeView view, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();

            jgen.writeStringField("key", view.getServiceName());
            jgen.writeStringField("type", "service");
            jgen.writeStringField("serviceName", view.getServiceName());

            jgen.writeArrayFieldStart("nodes");
            for (NodeView nodeView : view.getNodes()) {
                provider.defaultSerializeValue(nodeView, jgen);
            }
            jgen.writeEndArray();

            jgen.writeEndObject();
        }
    }
}