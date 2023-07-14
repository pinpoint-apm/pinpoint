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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;

/**
 * @author emeroad
 */
@JsonInclude(content = JsonInclude.Include.NON_NULL)
public class MapWrap {
    private final ApplicationMap applicationMap;

    public MapWrap(ApplicationMap applicationMap, final TimeHistogramFormat timeHistogramFormat) {
        this.applicationMap = applicationMap;
        if(timeHistogramFormat == TimeHistogramFormat.V2) {
            for(Node node : applicationMap.getNodes()) {
                node.setTimeHistogramFormat(timeHistogramFormat);
            }
            for(Link link : applicationMap.getLinks()) {
                link.setTimeHistogramFormat(timeHistogramFormat);
            }
        }
    }

    public void setV3Format(boolean v3Format) {
        for(Node node : applicationMap.getNodes()) {
            node.setV3Format(v3Format);
        }
        for(Link link : applicationMap.getLinks()) {
            link.setV3Format(v3Format);
        }
    }

    @JsonProperty("applicationMapData")
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

}
