/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author HyunGil Jeong
 */
public class LinkFactory {

    public enum LinkType {
        BASIC {
            @Override
            Link createLink(CreateType createType, Node fromNode, Node toNode, Range range) {
                Link delegate = DETAILED.createLink(createType, fromNode, toNode, range);
                return new BasicLink(delegate);
            }
        },
        DETAILED {
            @Override
            Link createLink(CreateType createType, Node fromNode, Node toNode, Range range) {
                return new DetailedLink(createType, fromNode, toNode, range);
            }
        };

        abstract Link createLink(CreateType createType, Node fromNode, Node toNode, Range range);
    }

    public static Link createLink(CreateType createType, Node fromNode, Node toNode, Range range) {
        return createLink(createType, fromNode, toNode, range, LinkType.DETAILED);
    }

    public static Link createLink(CreateType createType, Node fromNode, Node toNode, Range range, LinkType linkType) {
        return linkType.createLink(createType, fromNode, toNode, range);
    }
}
