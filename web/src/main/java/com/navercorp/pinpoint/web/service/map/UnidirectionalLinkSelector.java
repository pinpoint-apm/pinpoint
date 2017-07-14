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
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.SearchDepth;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public class UnidirectionalLinkSelector implements LinkSelector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationsMapCreator applicationsMapCreator;

    private final VirtualLinkProcessor virtualLinkProcessor;

    private final ServerMapDataFilter serverMapDataFilter;

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    UnidirectionalLinkSelector(
            ApplicationsMapCreator applicationsMapCreator,
            VirtualLinkProcessor virtualLinkProcessor,
            ServerMapDataFilter serverMapDataFilter) {
        if (applicationsMapCreator == null) {
            throw new NullPointerException("applicationsMapCreator must not be null");
        }
        if (virtualLinkProcessor == null) {
            throw new NullPointerException("virtualLinkProcessor must not be null");
        }
        this.applicationsMapCreator = applicationsMapCreator;
        this.virtualLinkProcessor = virtualLinkProcessor;
        this.serverMapDataFilter = serverMapDataFilter;
    }

    @Override
    public LinkDataDuplexMap select(Application sourceApplication, Range range, SearchOption searchOption) {
        if (searchOption == null) {
            throw new NullPointerException("searchOption must not be null");
        }

        logger.debug("Creating link data map for {}", sourceApplication);
        final SearchDepth callerDepth = new SearchDepth(searchOption.getCallerSearchDepth());
        final SearchDepth calleeDepth = new SearchDepth(searchOption.getCalleeSearchDepth());

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        List<Application> applications;
        if (!filter(sourceApplication)) {
            applications = Collections.emptyList();
        } else {
            applications = Collections.singletonList(sourceApplication);
        }
        List<Application> outboundApplications = Collections.unmodifiableList(applications);
        LinkSelectContext outboundLinkSelectContext = new LinkSelectContext(range, callerDepth, new SearchDepth(0), linkVisitChecker);
        List<Application> inboundApplications = Collections.unmodifiableList(applications);
        LinkSelectContext inboundLinkSelectContext = new LinkSelectContext(range, new SearchDepth(0), calleeDepth, linkVisitChecker);

        while (!outboundApplications.isEmpty() || !inboundApplications.isEmpty()) {

            logger.info("depth search start. callerDepth:{}, calleeDepth:{}, size:{}, nodes:{}", outboundLinkSelectContext.getCallerDepth(), inboundLinkSelectContext.getCalleeDepth(), applications.size(), applications);
            LinkDataDuplexMap outboundMap = applicationsMapCreator.createLinkDataDuplexMap(outboundApplications, outboundLinkSelectContext);
            LinkDataDuplexMap inboundMap = applicationsMapCreator.createLinkDataDuplexMap(inboundApplications, inboundLinkSelectContext);
            logger.info("depth search end. callerDepth:{}, calleeDepth:{}", outboundLinkSelectContext.getCallerDepth(), inboundLinkSelectContext.getCalleeDepth());

            linkDataDuplexMap.addLinkDataDuplexMap(outboundMap);
            linkDataDuplexMap.addLinkDataDuplexMap(inboundMap);

            outboundApplications = outboundLinkSelectContext.getNextApplications()
                    .stream()
                    .filter(this::filter)
                    .collect(Collectors.toList());
            inboundApplications = inboundLinkSelectContext.getNextApplications()
                    .stream()
                    .filter(this::filter)
                    .collect(Collectors.toList());

            outboundLinkSelectContext = outboundLinkSelectContext.advance();
            inboundLinkSelectContext = inboundLinkSelectContext.advance();
        }
        return virtualLinkProcessor.processVirtualLinks(linkDataDuplexMap, linkVisitChecker, range);
    }

    private boolean filter(Application targetApplication) {
        if (serverMapDataFilter != null && serverMapDataFilter.filter(targetApplication)) {
            return false;
        }
        return true;
    }
}
