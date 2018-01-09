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

import com.google.common.collect.Sets;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.Collection;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class SourceApplicationFilter implements LinkDataMapProcessor {

    private final Set<Application> sourceApplications;

    public SourceApplicationFilter(Application sourceApplication) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        this.sourceApplications = Sets.newHashSet(sourceApplication);
    }

    public SourceApplicationFilter(Collection<Application> sourceApplications) {
        if (sourceApplications == null) {
            throw new NullPointerException("sourceApplications must not be null");
        }
        this.sourceApplications = Sets.newHashSet(sourceApplications);
    }

    @Override
    public LinkDataMap processLinkDataMap(LinkDataMap linkDataMap, Range range) {
        final LinkDataMap filteredLinkDataMap = new LinkDataMap();
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            if (accept(linkData)) {
                filteredLinkDataMap.addLinkData(linkData);
            }
        }
        return filteredLinkDataMap;
    }

    private boolean accept(LinkData linkData) {
        final Application fromApplication = linkData.getFromApplication();
        if (sourceApplications.contains(fromApplication)) {
            return true;
        }
        return false;
    }
}
