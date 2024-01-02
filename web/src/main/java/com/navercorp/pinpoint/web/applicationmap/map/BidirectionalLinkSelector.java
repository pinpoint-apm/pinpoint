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
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.service.SearchDepth;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Breadth-first link search
 * not thread safe
 *
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class BidirectionalLinkSelector implements LinkSelector {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationsMapCreator applicationsMapCreator;

    private final VirtualLinkHandler virtualLinkHandler;

    private final ServerMapDataFilter serverMapDataFilter;

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    BidirectionalLinkSelector(
            ApplicationsMapCreator applicationsMapCreator,
            VirtualLinkHandler virtualLinkHandler,
            ServerMapDataFilter serverMapDataFilter) {
        this.applicationsMapCreator = Objects.requireNonNull(applicationsMapCreator, "applicationsMapCreator");
        this.virtualLinkHandler = Objects.requireNonNull(virtualLinkHandler, "virtualLinkHandler");
        this.serverMapDataFilter = serverMapDataFilter;
    }

    @Override
    public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int outSearchDepth, int inSearchDepth) {
        return select(sourceApplications, range, outSearchDepth, inSearchDepth, false);
    }

    @Override
    public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int outSearchDepth, int inSearchDepth, boolean timeAggregated) {
        logger.debug("Creating link data map for {}", sourceApplications);
        final SearchDepth outDepth = new SearchDepth(outSearchDepth);
        final SearchDepth inDepth = new SearchDepth(inSearchDepth);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        List<Application> applications = filterApplications(sourceApplications);
        LinkSelectContext linkSelectContext = new LinkSelectContext(range, outDepth, inDepth, linkVisitChecker, timeAggregated);

        while (!applications.isEmpty()) {

            logger.info("depth search start. depth:{} -> {}, size:{}, nodes:{}", linkSelectContext.getOutDepth(), linkSelectContext.getInDepth(), applications.size(), applications);
            LinkDataDuplexMap levelData = applicationsMapCreator.createLinkDataDuplexMap(applications, linkSelectContext);
            logger.info("depth search end. depth:{} -> {}", linkSelectContext.getOutDepth(), linkSelectContext.getInDepth());

            linkDataDuplexMap.addLinkDataDuplexMap(levelData);

            List<Application> nextApplications = linkSelectContext.getNextApplications();
            applications = filterApplications(nextApplications);
            linkSelectContext = linkSelectContext.advance();
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
