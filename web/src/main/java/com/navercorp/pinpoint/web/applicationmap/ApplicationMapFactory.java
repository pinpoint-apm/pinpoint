/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;

import java.util.Collection;

/**
 * Node map
 * 
 * @author netspider
 * @author emeroad
 */
public class ApplicationMapFactory {

    public static LinkList filterEmptyLink(LinkList originalLinkList) {
        if (originalLinkList.isEmpty()) {
            return LinkList.of();
        }

        Collection<Link> linkList = originalLinkList.getLinkList();
        LinkList.Builder builder = LinkList.newBuilder(linkList.size());
        for (Link link : linkList) {
            if (link == null) {
                continue;
            }
            if (link.getHistogram().getTotalCount() == 0) {
                continue;
            }
            builder.addLink(link);
        }

        return builder.build();
    }

    public static ApplicationMap build(NodeList nodeList, LinkList linkList, Range range) {
        LinkList links = filterEmptyLink(linkList);
        return new SimpleApplicationMap(nodeList, links, range);
    }

}
