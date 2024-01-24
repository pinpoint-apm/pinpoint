/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class LinkDataMapProcessors implements LinkDataMapProcessor {

    private final LinkDataMapProcessor[] linkDataMapProcessors;

    public LinkDataMapProcessors(List<LinkDataMapProcessor> linkDataMapProcessors) {
        Objects.requireNonNull(linkDataMapProcessors, "linkDataMapProcessors");
        this.linkDataMapProcessors = linkDataMapProcessors.toArray(new LinkDataMapProcessor[0]);
    }

    @Override
    public LinkDataMap processLinkDataMap(LinkDirection linkDirection, LinkDataMap linkDataMap, Range range) {
        LinkDataMap processedLinkDataMap = linkDataMap;
        for (LinkDataMapProcessor linkDataMapProcessor : linkDataMapProcessors) {
            processedLinkDataMap = linkDataMapProcessor.processLinkDataMap(linkDirection, processedLinkDataMap, range);
        }
        return processedLinkDataMap;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final List<LinkDataMapProcessor> linkDataMapProcessors = new ArrayList<>();

        Builder() {
        }

        public void addLinkProcessor(LinkDataMapProcessor linkProcessor) {
            Objects.requireNonNull(linkProcessor, "linkProcessor");
            if (LinkDataMapProcessor.NO_OP == linkProcessor) {
                return;
            }
            linkDataMapProcessors.add(linkProcessor);
        }

        public LinkDataMapProcessors build() {
            return new LinkDataMapProcessors(linkDataMapProcessors);
        }
    }
}
