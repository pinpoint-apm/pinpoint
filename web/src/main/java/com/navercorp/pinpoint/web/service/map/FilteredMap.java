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

package com.navercorp.pinpoint.web.service.map;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.service.DotExtractor;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseHistograms;
import com.navercorp.pinpoint.web.vo.scatter.ApplicationScatterScanResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class FilteredMap {

    private final LinkDataDuplexMap linkDataDuplexMap;
    private ResponseHistograms responseHistograms;
    private DotExtractor dotExtractor;

    FilteredMap(LinkDataDuplexMap linkDataDuplexMap, ResponseHistograms responseHistograms, DotExtractor dotExtractor) {
        this.linkDataDuplexMap = Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");
        this.responseHistograms = Objects.requireNonNull(responseHistograms, "responseHistograms");
        this.dotExtractor = Objects.requireNonNull(dotExtractor, "dotExtractor");
    }

    public LinkDataDuplexMap getLinkDataDuplexMap() {
        return linkDataDuplexMap;
    }

    public ResponseHistograms getResponseHistograms() {
        return responseHistograms;
    }

    public List<ApplicationScatterScanResult> getApplicationScatterScanResult(long from, long to) {
        return dotExtractor.getApplicationScatterScanResult(from, to);
    }

    public Map<Application, ScatterData> getApplicationScatterData(long from, long to, int xGroupUnitMillis, int yGroupUnitMillis) {
        return dotExtractor.getApplicationScatterData(from, to, xGroupUnitMillis, yGroupUnitMillis);
    }
}
