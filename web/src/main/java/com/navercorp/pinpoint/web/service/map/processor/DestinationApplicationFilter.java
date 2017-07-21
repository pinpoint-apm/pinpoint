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
public class DestinationApplicationFilter implements LinkDataMapProcessor {

    private final Set<Application> destinationApplications;

    public DestinationApplicationFilter(Application destinationApplication) {
        if (destinationApplication == null) {
            throw new NullPointerException("destinationApplication must not be null");
        }
        this.destinationApplications = Sets.newHashSet(destinationApplication);
    }

    public DestinationApplicationFilter(Collection<Application> destinationApplications) {
        if (destinationApplications == null) {
            throw new NullPointerException("destinationApplications must not be null");
        }
        this.destinationApplications = Sets.newHashSet(destinationApplications);
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
        final Application toApplication = linkData.getToApplication();
        if (destinationApplications.contains(toApplication)) {
            return true;
        }
        return false;
    }
}
