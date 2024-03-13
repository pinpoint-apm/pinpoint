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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
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

    private final LinkDataMapProcessor outLinkDataMapProcessor;

    private final LinkDataMapProcessor inLinkDataMapProcessor;

    public DefaultApplicationMapCreator(LinkDataMapService linkDataMapService, LinkDataMapProcessor outLinkDataMapProcessor, LinkDataMapProcessor inLinkDataMapProcessor) {
        this.linkDataMapService = Objects.requireNonNull(linkDataMapService, "linkDataMapService");
        this.outLinkDataMapProcessor = Objects.requireNonNull(outLinkDataMapProcessor, "outLinkDataMapProcessor");
        this.inLinkDataMapProcessor = Objects.requireNonNull(inLinkDataMapProcessor, "inLinkDataMapProcessor");
    }

    @Override
    public LinkDataDuplexMap createMap(Application application, LinkSelectContext linkSelectContext) {
        logger.debug("Finding Out/In link data for {}", application);
        final Range range = linkSelectContext.getRange();
        LinkDataDuplexMap searchResult = new LinkDataDuplexMap();

        if (linkSelectContext.checkNextOut(application)) {
            final LinkDataMap outLinkDataMap = linkDataMapService.selectCallerLinkDataMap(application, range, linkSelectContext.isTimeAggregated());
            logger.debug("Found {}. node={}, depth={}, count={}", LinkDirection.OUT_LINK, application, linkSelectContext.getOutDepth(), outLinkDataMap.size());

            final LinkDataMap processedOutLinkDataMap = outLinkDataMapProcessor.processLinkDataMap(LinkDirection.OUT_LINK, outLinkDataMap, range);
            logger.debug("Processed {} node={} count:{} {}", LinkDirection.OUT_LINK, application, processedOutLinkDataMap.size(), processedOutLinkDataMap);
            for (LinkData outLinkData : processedOutLinkDataMap.getLinkDataList()) {
                searchResult.addSourceLinkData(outLinkData);
                final Application toApplication = outLinkData.getToApplication();
                // skip if nextApplication is a terminal or an unknown cloud
                final ServiceType toServiceType = toApplication.serviceType();
                if (toServiceType.isTerminal() || toServiceType.isUnknown()) {
                    continue;
                }
                linkSelectContext.addNextApplication(toApplication);
            }
        }

        if (linkSelectContext.checkNextIn(application)) {
            final LinkDataMap inLinkDataMap = linkDataMapService.selectCalleeLinkDataMap(application, range, linkSelectContext.isTimeAggregated());
            logger.debug("Found {}. node={}, depth={}, count={}", LinkDirection.IN_LINK, application, linkSelectContext.getInDepth(), inLinkDataMap.size());

            final LinkDataMap processedInLinkDataMap = inLinkDataMapProcessor.processLinkDataMap(LinkDirection.IN_LINK, inLinkDataMap, range);
            logger.debug("Processed {} node={} count:{} {}", LinkDirection.IN_LINK, application, processedInLinkDataMap.size(), processedInLinkDataMap);
            for (LinkData inLinkData : processedInLinkDataMap.getLinkDataList()) {
                searchResult.addTargetLinkData(inLinkData);
                final Application fromApplication = inLinkData.getFromApplication();
                linkSelectContext.addNextApplication(fromApplication);
            }
        }
        return searchResult;
    }
}
