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
import com.navercorp.pinpoint.web.applicationmap.view.LinkView;
import org.springframework.boot.jackson.JacksonComponent;

import java.util.List;
import java.util.Objects;

@JsonSerialize(using = ServiceGroupLinkView.ServiceGroupLinkViewSerializer.class)
public class ServiceGroupLinkView implements LinkViewEntry {
    private final LinkNodeKey from;
    private final LinkNodeKey to;
    private final List<LinkView> links;

    public ServiceGroupLinkView(LinkNodeKey from, LinkNodeKey to, List<LinkView> links) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.links = Objects.requireNonNull(links, "links");
    }

    public LinkNodeKey getFrom() {
        return from;
    }

    public LinkNodeKey getTo() {
        return to;
    }

    public List<LinkView> getLinks() {
        return links;
    }

    @JacksonComponent
    public static class ServiceGroupLinkViewSerializer extends ValueSerializer<ServiceGroupLinkView> {

        @Override
        public void serialize(ServiceGroupLinkView view, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeStartObject();

            jgen.writeStringProperty("key", view.getFrom().key() + "~" + view.getTo().key());
            jgen.writeStringProperty("from", view.getFrom().key());
            jgen.writeStringProperty("to", view.getTo().key());
            jgen.writeStringProperty("type", "service");

            jgen.writeArrayPropertyStart("links");
            for (LinkView linkView : view.getLinks()) {
                provider.writeValue(jgen, linkView);
            }
            jgen.writeEndArray();

            jgen.writeEndObject();
        }
    }
}
