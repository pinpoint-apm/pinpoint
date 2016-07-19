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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class ApplicationMapWithScatterData implements ApplicationMap {

    private final ApplicationMap applicationMap;
    private final Map<Application, ScatterData> applicationScatterDataMap;

    public ApplicationMapWithScatterData(ApplicationMap applicationMap, Map<Application, ScatterData> applicationScatterDataMap) {
        this.applicationMap = applicationMap;
        this.applicationScatterDataMap = applicationScatterDataMap;
    }

    @JsonProperty("nodeDataArray")
    @Override
    public Collection<Node> getNodes() {
        return applicationMap.getNodes();
    }

    @JsonProperty("linkDataArray")
    @Override
    public Collection<Link> getLinks() {
        return applicationMap.getLinks();
    }
    
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

    @JsonIgnore
    public Map<Application, ScatterData> getApplicationScatterDataMap() {
        return applicationScatterDataMap;
    }

}
