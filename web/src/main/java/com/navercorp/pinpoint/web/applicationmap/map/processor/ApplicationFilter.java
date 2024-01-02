/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ApplicationFilter implements LinkDataMapProcessor {

    private final Application application;

    public ApplicationFilter(Application application) {
        this.application = Objects.requireNonNull(application, "application");
    }

    @Override
    public LinkDataMap processLinkDataMap(LinkDirection linkDirection, LinkDataMap linkDataMap, Range range) {
        Objects.requireNonNull(linkDataMap, "linkDataMap");

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
        final Application toApplication = linkData.getToApplication();
        if (application.equals(fromApplication) || application.equals(toApplication)) {
            return true;
        }
        return false;
    }
}
