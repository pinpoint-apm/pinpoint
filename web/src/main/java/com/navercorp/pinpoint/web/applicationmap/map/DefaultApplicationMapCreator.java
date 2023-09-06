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

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.applicationmap.service.LinkDataMapService;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class DefaultApplicationMapCreator implements ApplicationMapCreator {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkDataMapService linkDataMapService;

    private final LinkDataMapProcessor callerLinkDataMapProcessor;

    private final LinkDataMapProcessor calleeLinkDataMapProcessor;

    public DefaultApplicationMapCreator(LinkDataMapService linkDataMapService, LinkDataMapProcessor callerLinkDataMapProcessor, LinkDataMapProcessor calleeLinkDataMapProcessor) {
        this.linkDataMapService = Objects.requireNonNull(linkDataMapService, "linkDataMapService");
        this.callerLinkDataMapProcessor = Objects.requireNonNull(callerLinkDataMapProcessor, "callerLinkDataMapProcessor");
        this.calleeLinkDataMapProcessor = Objects.requireNonNull(calleeLinkDataMapProcessor, "calleeLinkDataMapProcessor");
    }

    @Override
    public LinkDataDuplexMap createMap(Application application, LinkSelectContext linkSelectContext) {
        logger.debug("Finding Caller/Callee link data for {}", application);
        final Range range = linkSelectContext.getRange();
        LinkDataDuplexMap searchResult = new LinkDataDuplexMap();

        final boolean searchCallerNode = linkSelectContext.checkNextCaller(application);
        if (searchCallerNode) {
            logger.debug("Finding Caller link data for {}", application);
            final LinkDataMap callerLinkDataMap = linkDataMapService.selectCallerLinkDataMap(application, range, linkSelectContext.isTimeAggregated());
            logger.debug("Found Caller. count={}, caller={}, depth={}", callerLinkDataMap.size(), application, linkSelectContext.getCallerDepth());

            final LinkDataMap processedCallerLinkDataMap = callerLinkDataMapProcessor.processLinkDataMap(callerLinkDataMap, range);
            for (LinkData callerLinkData : processedCallerLinkDataMap.getLinkDataList()) {
                searchResult.addSourceLinkData(callerLinkData);
                final Application toApplication = callerLinkData.getToApplication();
                // skip if nextApplication is a terminal or an unknown cloud
                if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
                    continue;
                }
                linkSelectContext.addNextApplication(toApplication);
            }
        }

        final boolean searchCalleeNode = linkSelectContext.checkNextCallee(application);
        if (searchCalleeNode) {
            logger.debug("Finding Callee link data for {}", application);
            final LinkDataMap calleeLinkDataMap = linkDataMapService.selectCalleeLinkDataMap(application, range, linkSelectContext.isTimeAggregated());
            logger.debug("Found Callee. count={}, callee={}, depth={}", calleeLinkDataMap.size(), application, linkSelectContext.getCalleeDepth());

            final LinkDataMap processedCalleeLinkDataMap = calleeLinkDataMapProcessor.processLinkDataMap(calleeLinkDataMap, range);
            for (LinkData calleeLinkData : processedCalleeLinkDataMap.getLinkDataList()) {
                searchResult.addTargetLinkData(calleeLinkData);
                final Application fromApplication = calleeLinkData.getFromApplication();
                linkSelectContext.addNextApplication(fromApplication);
            }
        }
        return searchResult;
    }
}
