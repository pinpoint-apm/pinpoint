/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;

import java.util.Collection;

/**
 * @author Taejin Koo
 */
public interface ApplicationMap {


    @JsonProperty("nodeDataArray")
    Collection<Node> getNodes();

    @JsonProperty("linkDataArray")
    Collection<Link> getLinks();

}
