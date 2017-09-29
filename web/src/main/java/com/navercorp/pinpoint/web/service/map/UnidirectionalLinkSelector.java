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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class UnidirectionalLinkSelector implements LinkSelector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationsMapCreator applicationsMapCreator;

    private final VirtualLinkHandler virtualLinkHandler;

    private final ServerMapDataFilter serverMapDataFilter;

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    UnidirectionalLinkSelector(
            ApplicationsMapCreator applicationsMapCreator,
            VirtualLinkHandler virtualLinkHandler,
            ServerMapDataFilter serverMapDataFilter) {
        if (applicationsMapCreator == null) {
            throw new NullPointerException("applicationsMapCreator must not be null");
        }
        if (virtualLinkHandler == null) {
            throw new NullPointerException("virtualLinkProcessor must not be null");
        }
        this.applicationsMapCreator = applicationsMapCreator;
        this.virtualLinkHandler = virtualLinkHandler;
        this.serverMapDataFilter = serverMapDataFilter;
    }

    @Override
    public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int callerSearchDepth, int calleeSearchDepth) {
        logger.debug("Creating link data map for {}", sourceApplications);
        final SearchDepth callerDepth = new SearchDepth(callerSearchDepth);
        final SearchDepth calleeDepth = new SearchDepth(calleeSearchDepth);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        List<Application> applications = filterApplications(sourceApplications);

        List<Application> outboundApplications = Collections.unmodifiableList(applications);
        LinkSelectContext outboundLinkSelectContext = new LinkSelectContext(range, callerDepth, new SearchDepth(0), linkVisitChecker);
        List<Application> inboundApplications = Collections.unmodifiableList(applications);
        LinkSelectContext inboundLinkSelectContext = new LinkSelectContext(range, new SearchDepth(0), calleeDepth, linkVisitChecker);

        while (!outboundApplications.isEmpty() || !inboundApplications.isEmpty()) {

            logger.info("outbound depth search start. callerDepth:{}, calleeDepth:{}, size:{}, nodes:{}", outboundLinkSelectContext.getCallerDepth(), outboundLinkSelectContext.getCalleeDepth(), outboundApplications.size(), outboundApplications);
            LinkDataDuplexMap outboundMap = applicationsMapCreator.createLinkDataDuplexMap(outboundApplications, outboundLinkSelectContext);
            logger.info("outbound depth search end. callerDepth:{}, calleeDepth:{}", outboundLinkSelectContext.getCallerDepth(), outboundLinkSelectContext.getCalleeDepth());

            logger.info("inbound depth search start. callerDepth:{}, calleeDepth:{}, size:{}, nodes:{}", inboundLinkSelectContext.getCallerDepth(), inboundLinkSelectContext.getCalleeDepth(), inboundApplications.size(), inboundApplications);
            LinkDataDuplexMap inboundMap = applicationsMapCreator.createLinkDataDuplexMap(inboundApplications, inboundLinkSelectContext);
            logger.info("inbound depth search end. callerDepth:{}, calleeDepth:{}", inboundLinkSelectContext.getCallerDepth(), inboundLinkSelectContext.getCalleeDepth());

            linkDataDuplexMap.addLinkDataDuplexMap(outboundMap);
            linkDataDuplexMap.addLinkDataDuplexMap(inboundMap);

            outboundApplications = filterApplications(outboundLinkSelectContext.getNextApplications());
            inboundApplications = filterApplications(inboundLinkSelectContext.getNextApplications());

            outboundLinkSelectContext = outboundLinkSelectContext.advance();
            inboundLinkSelectContext = inboundLinkSelectContext.advance();
        }
        return virtualLinkHandler.processVirtualLinks(linkDataDuplexMap, linkVisitChecker, range);
    }

    private List<Application> filterApplications(List<Application> applications) {
        if (serverMapDataFilter == null) {
            return applications;
        }
        List<Application> filteredApplications = new ArrayList<>();
        for (Application application : applications) {
            if (!serverMapDataFilter.filter(application)) {
                filteredApplications.add(application);
            }
        }
        return filteredApplications;
    }
}
