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

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;
import org.springframework.boot.jackson.JacksonComponent;

import java.util.ArrayList;
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

    /**
     * Apdex score of this group, merged from the scores of the nodes it collapses.
     */
    public ApdexScore getApdexScore() {
        final List<ApdexScore> apdexScores = new ArrayList<>(nodes.size());
        for (NodeView nodeView : nodes) {
            apdexScores.add(nodeView.getNode().getApdexScore());
        }
        return ApdexScore.newApdexScore(apdexScores);
    }

    @JacksonComponent
    public static class ServiceGroupNodeViewSerializer extends ValueSerializer<ServiceGroupNodeView> {

        @Override
        public void serialize(ServiceGroupNodeView view, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeStartObject();

            jgen.writeStringProperty("key", view.getServiceName());
            jgen.writeStringProperty("type", "service");
            jgen.writeStringProperty("serviceName", view.getServiceName());

            jgen.writePOJOProperty("apdex", view.getApdexScore());

            jgen.writeArrayPropertyStart("nodes");
            for (NodeView nodeView : view.getNodes()) {
                provider.writeValue(jgen, nodeView);
            }
            jgen.writeEndArray();

            jgen.writeEndObject();
        }
    }
}