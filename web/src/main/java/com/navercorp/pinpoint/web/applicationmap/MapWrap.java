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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author emeroad
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class MapWrap {
    private final ApplicationMap applicationMap;

    public MapWrap(ApplicationMap applicationMap) {
        this.applicationMap = applicationMap;
    }

    @JsonProperty("applicationMapData")
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

}
