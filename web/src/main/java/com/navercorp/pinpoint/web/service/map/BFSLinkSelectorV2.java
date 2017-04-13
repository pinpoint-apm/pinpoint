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

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.SearchDepth;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Breadth-first link search
 * not thread safe
 *
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class BFSLinkSelectorV2 implements LinkSelector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkDataMapCreator linkDataMapCreator;

    private final VirtualLinkHandler virtualLinkHandler;

    private final ServerMapDataFilter serverMapDataFilter;

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    private final Queue nextQueue = new Queue();

    BFSLinkSelectorV2(
            LinkDataMapCreator linkDataMapCreator,
            VirtualLinkHandler virtualLinkHandler,
            ServerMapDataFilter serverMapDataFilter) {
        if (linkDataMapCreator == null) {
            throw new NullPointerException("linkDataMapCreator must not be null");
        }
        if (virtualLinkHandler == null) {
            throw new NullPointerException("virtualLinkProcessor must not be null");
        }
        this.linkDataMapCreator = linkDataMapCreator;
        this.virtualLinkHandler = virtualLinkHandler;
        this.serverMapDataFilter = serverMapDataFilter;
    }

    public LinkDataDuplexMap select(Application sourceApplication, Range range, SearchOption searchOption) {
        if (searchOption == null) {
            throw new NullPointerException("searchOption must not be null");
        }

        SearchDepth callerDepth = new SearchDepth(searchOption.getCallerSearchDepth());
        SearchDepth calleeDepth = new SearchDepth(searchOption.getCalleeSearchDepth());

        logger.debug("ApplicationMap select {}", sourceApplication);
        addNextNode(sourceApplication);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();

        while (!this.nextQueue.isEmpty()) {

            final List<Application> currentNode = this.nextQueue.copyAndClear();

            logger.debug("size:{} depth caller:{} callee:{} node:{}", currentNode.size(), callerDepth.getDepth(), calleeDepth.getDepth(), currentNode);
            LinkDataDuplexMap levelData = selectLink(currentNode, range, callerDepth, calleeDepth);

            linkDataDuplexMap.addLinkDataDuplexMap(levelData);

            callerDepth = callerDepth.nextDepth();
            calleeDepth = calleeDepth.nextDepth();
        }

        return virtualLinkHandler.processVirtualLinks(linkDataDuplexMap, linkVisitChecker, range);
    }

    /**
     * Queries for all applications(caller&callee) called by the targetApplicationList
     *
     * @param targetApplicationList
     * @param range
     * @return
     */
    private LinkDataDuplexMap selectLink(List<Application> targetApplicationList, Range range, SearchDepth callerDepth, SearchDepth calleeDepth) {

        final LinkDataDuplexMap searchResult = new LinkDataDuplexMap();

        for (Application targetApplication : targetApplicationList) {
            final boolean searchCallerNode = checkNextCaller(targetApplication, callerDepth);
            if (searchCallerNode) {
                final LinkDataMap callerLinkDataMap = linkDataMapCreator.createCallerLinkDataMap(targetApplication, range);
                logger.debug("Found Caller. count={}, caller={}, depth={}", callerLinkDataMap.size(), targetApplication, callerDepth.getDepth());

                for (LinkData callerLinkData : callerLinkDataMap.getLinkDataList()) {
                    searchResult.addSourceLinkData(callerLinkData);
                    final Application toApplication = callerLinkData.getToApplication();
                    // skip if nextApplication is a terminal or an unknown cloud
                    if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
                        continue;
                    }
                    addNextNode(toApplication);
                }
            }

            final boolean searchCalleeNode = checkNextCallee(targetApplication, calleeDepth);
            if (searchCalleeNode) {
                final LinkDataMap calleeLinkDataMap = linkDataMapCreator.createCalleeLinkDataMap(targetApplication, range);
                logger.debug("Found Callee. count={}, callee={}, depth={}", calleeLinkDataMap.size(), targetApplication, calleeDepth.getDepth());

                for (LinkData calleeLinkData : calleeLinkDataMap.getLinkDataList()) {
                    searchResult.addTargetLinkData(calleeLinkData);
                    final Application fromApplication = calleeLinkData.getFromApplication();
                    addNextNode(fromApplication);
                }
            }
        }
        logger.debug("{} depth search end", callerDepth.getDepth());
        return searchResult;
    }

    private void addNextNode(Application sourceApplication) {
        final boolean add = this.nextQueue.addNextNode(sourceApplication);
        if (!add) {
            logger.debug("already visited. nextNode:{}", sourceApplication);
        }
    }

    private boolean checkNextCaller(Application targetApplication, SearchDepth depth) {
        if (depth.isDepthOverflow()) {
            logger.debug("caller depth overflow application:{} depth:{}", targetApplication, depth.getDepth());
            return false;
        }
        if (linkVisitChecker.visitCaller(targetApplication)) {
            logger.debug("already visited caller:{}", targetApplication);
            return false;
        }
        return filter(targetApplication);
    }

    private boolean checkNextCallee(Application targetApplication, SearchDepth depth) {
        if (depth.isDepthOverflow()) {
            logger.debug("callee depth overflow application:{} depth:{}", targetApplication, depth.getDepth());
            return false;
        }
        if (linkVisitChecker.visitCallee(targetApplication)) {
            logger.debug("already visited callee:{}", targetApplication);
            return false;
        }
        return filter(targetApplication);
    }

    private boolean filter(Application targetApplication) {
        if (serverMapDataFilter != null && serverMapDataFilter.filter(targetApplication)) {
            return false;
        }
        return true;
    }

    static class Queue {

        private final Set<Application> nextNode = new HashSet<>();

        public boolean addNextNode(Application application) {
            return this.nextNode.add(application);
        }

        public List<Application> copyAndClear() {
            List<Application> copyList = new ArrayList<>(this.nextNode);

            this.nextNode.clear();

            return copyList;
        }

        public boolean isEmpty() {
            return this.nextNode.isEmpty();
        }

    }

}
