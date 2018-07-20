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

package com.navercorp.pinpoint.web.service.map.processor;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class LinkDataMapProcessors implements LinkDataMapProcessor {

    private List<LinkDataMapProcessor> linkDataMapProcessors = new ArrayList<>();

    public void addLinkDataMapProcessor(LinkDataMapProcessor linkDataMapProcessor) {
        if (linkDataMapProcessor == null) {
            return;
        }
        linkDataMapProcessors.add(linkDataMapProcessor);
    }

    @Override
    public LinkDataMap processLinkDataMap(LinkDataMap linkDataMap, Range range) {
        LinkDataMap processedLinkDataMap = linkDataMap;
        for (LinkDataMapProcessor linkDataMapProcessor : linkDataMapProcessors) {
            processedLinkDataMap = linkDataMapProcessor.processLinkDataMap(processedLinkDataMap, range);
        }
        return processedLinkDataMap;
    }
}
