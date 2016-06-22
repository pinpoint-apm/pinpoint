/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.*;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.service.map.AcceptApplicationLocalCache;
import com.navercorp.pinpoint.web.service.map.RpcApplication;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;

import com.navercorp.pinpoint.web.vo.SearchOption;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Breadth-first link search
 * not thread safe
 * @author emeroad
 * @author minwoo.jung
 */
public class BFSLinkSelector implements LinkSelector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    private final MapStatisticsCalleeDao mapStatisticsCalleeDao;

    private final MapStatisticsCallerDao mapStatisticsCallerDao;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final AcceptApplicationLocalCache acceptApplicationLocalCache = new AcceptApplicationLocalCache();

    private final Set<LinkData> emulationLinkMarker = new HashSet<>();

    private final Queue nextQueue = new Queue();
    
    private ServerMapDataFilter serverMapDataFilter;

    public BFSLinkSelector(MapStatisticsCallerDao mapStatisticsCallerDao, MapStatisticsCalleeDao mapStatisticsCalleeDao, HostApplicationMapDao hostApplicationMapDao, ServerMapDataFilter serverMapDataFilter) {
        if (mapStatisticsCalleeDao == null) {
            throw new NullPointerException("mapStatisticsCalleeDao must not be null");
        }
        if (mapStatisticsCallerDao == null) {
            throw new NullPointerException("mapStatisticsCallerDao must not be null");
        }
        if (hostApplicationMapDao == null) {
            throw new NullPointerException("hostApplicationMapDao must not be null");
        }
        this.mapStatisticsCalleeDao = mapStatisticsCalleeDao;
        this.mapStatisticsCallerDao = mapStatisticsCallerDao;
        this.hostApplicationMapDao = hostApplicationMapDao;
        this.serverMapDataFilter = serverMapDataFilter;
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
                final LinkDataMap caller = mapStatisticsCallerDao.selectCaller(targetApplication, range);
                if (logger.isDebugEnabled()) {
                    logger.debug("Found Caller. count={}, caller={}, depth={}", caller.size(), targetApplication, callerDepth.getDepth());
                }

                final LinkDataMap replaceRpcCaller = replaceRpcCaller(caller, range);

                for (LinkData link : replaceRpcCaller.getLinkDataList()) {
                    searchResult.addSourceLinkData(link);

                    final Application toApplication = link.getToApplication();
                    // skip if nextApplication is a terminal or an unknown cloud
                    if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
                        continue;
                    }

                    addNextNode(toApplication);
                }
            }

            final boolean searchCalleeNode = checkNextCallee(targetApplication, calleeDepth);
            if (searchCalleeNode) {
                final LinkDataMap callee = mapStatisticsCalleeDao.selectCallee(targetApplication, range);
                if (logger.isInfoEnabled()) {
                    logger.debug("Found Callee. count={}, callee={}, depth={}", callee.size(), targetApplication, calleeDepth.getDepth());
                }
                for (LinkData stat : callee.getLinkDataList()) {
                    
                    searchResult.addTargetLinkData(stat);

                    final Application fromApplication = stat.getFromApplication();
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
    
    private boolean filter(Application targetApplication) {
        if (serverMapDataFilter != null && serverMapDataFilter.filter(targetApplication)) {
          return false;
        }
            
        return true;
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



    private List<LinkData> checkRpcCallAccepted(LinkData linkData, Range range) {
        // replace if the rpc client's destination has an agent installed and thus has an application name
        final Application toApplication = linkData.getToApplication();
        if (!toApplication.getServiceType().isRpcClient() && !toApplication.getServiceType().isQueue()) {
            return Collections.singletonList(linkData);
        }

        logger.debug("checkRpcCallAccepted(). Find applicationName:{} {}", toApplication, range);

        final Set<AcceptApplication> acceptApplicationList = findAcceptApplication(linkData.getFromApplication(), toApplication.getName(), range);
        logger.debug("find accept application:{}", acceptApplicationList);
        if (CollectionUtils.isNotEmpty(acceptApplicationList)) {
            if (acceptApplicationList.size() == 1) {
                logger.debug("Application info replaced. {} => {}", linkData, acceptApplicationList);

                AcceptApplication first = acceptApplicationList.iterator().next();
                final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), first.getApplication());
                acceptedLinkData.setLinkCallDataMap(linkData.getLinkCallDataMap());
                return Collections.singletonList(acceptedLinkData);
            } else {
                // special case - there are more than 2 nodes grouped by a single url
                return createVirtualLinkData(linkData, toApplication, acceptApplicationList);
            }
        } else {
            // for queues, accept application may not exist if no consumers have an agent installed
            if (toApplication.getServiceType().isQueue()) {
                return Collections.singletonList(linkData);
            } else {
                final Application unknown = new Application(toApplication.getName(), ServiceType.UNKNOWN);
                final LinkData unknownLinkData = new LinkData(linkData.getFromApplication(), unknown);
                unknownLinkData.setLinkCallDataMap(linkData.getLinkCallDataMap());
                return Collections.singletonList(unknownLinkData);
            }
        }

    }

    private List<LinkData> createVirtualLinkData(LinkData linkData, Application toApplication, Set<AcceptApplication> acceptApplicationList) {
        logger.warn("one to N replaced. node:{}->host:{} accept:{}", linkData.getFromApplication(), toApplication.getName(), acceptApplicationList);

        List<LinkData> emulationLink = new ArrayList<>();
        for (AcceptApplication acceptApplication : acceptApplicationList) {
            // linkCallData needs to be modified - remove callHistogram on purpose
            final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), acceptApplication.getApplication());
            acceptedLinkData.setLinkCallDataMap(linkData.getLinkCallDataMap());
            emulationLink.add(acceptedLinkData);
            traceEmulationLink(acceptedLinkData);
        }
        return emulationLink;
    }

    private void traceEmulationLink(LinkData acceptApplication) {
        final boolean add = emulationLinkMarker.add(acceptApplication);
        if (!add) {
            logger.warn("emulationLink add error. {}", acceptApplication);
        }
    }


    private Set<AcceptApplication> findAcceptApplication(Application fromApplication, String host, Range range) {
        logger.debug("findAcceptApplication {} {}", fromApplication, host);

        final RpcApplication rpcApplication = new RpcApplication(host, fromApplication);
        final Set<AcceptApplication> hit = this.acceptApplicationLocalCache.get(rpcApplication);
        if (CollectionUtils.isNotEmpty(hit)) {
            logger.debug("acceptApplicationLocalCache hit {}", rpcApplication);
            return hit;
        }
        final Set<AcceptApplication> acceptApplicationSet= hostApplicationMapDao.findAcceptApplicationName(fromApplication, range);
        this.acceptApplicationLocalCache.put(rpcApplication, acceptApplicationSet);

        Set<AcceptApplication> acceptApplication = this.acceptApplicationLocalCache.get(rpcApplication);
        logger.debug("findAcceptApplication {}->{} result:{}", fromApplication, host, acceptApplication);
        return acceptApplication;
    }

    private void fillEmulationLink(LinkDataDuplexMap linkDataDuplexMap, Range range) {
        // TODO need to be reimplemented - virtual node creation logic needs an overhaul.
        // Currently, only the reversed relationship node is displayed. We need to create a virtual node and convert the rpc data appropriately.
        logger.debug("this.emulationLinkMarker:{}", this.emulationLinkMarker);
        List<LinkData> emulationLinkDataList = findEmulationLinkData(linkDataDuplexMap);

        for (LinkData emulationLinkData : emulationLinkDataList) {
            LinkCallDataMap beforeImage = emulationLinkData.getLinkCallDataMap();
            logger.debug("beforeImage:{}", beforeImage);
            emulationLinkData.resetLinkData();

            LinkKey findLinkKey = new LinkKey(emulationLinkData.getFromApplication(), emulationLinkData.getToApplication());
            LinkData targetLinkData = linkDataDuplexMap.getTargetLinkData(findLinkKey);
            if (targetLinkData == null) {
                // This is a case where the emulation target node has been only "partially" visited, (ie. does not have a target link data)
                // Most likely due to the limit imposed by inbound search depth.
                // Must go fetch the target link data here.
                final Application targetApplication = emulationLinkData.getToApplication();
                final LinkDataMap callee = mapStatisticsCalleeDao.selectCallee(targetApplication, range);
                targetLinkData = callee.getLinkData(findLinkKey);
                if (targetLinkData == null) {
                    // There has been a case where targetLinkData was null, but exact event could not be captured for analysis.
                    // Logging the case for further analysis should it happen again in the future.
                    logger.error("targetLinkData not found findLinkKey:{}", findLinkKey);
                    continue;
                }
            }

            // create reversed link data - convert data accepted by the target to target's call data
            LinkCallDataMap targetList = targetLinkData.getLinkCallDataMap();
            Collection<LinkCallData> beforeLinkDataList = beforeImage.getLinkDataList();

            LinkCallData beforeLinkCallData = beforeLinkDataList.iterator().next();
            for (LinkCallData agentHistogram : targetList.getLinkDataList()) {
                Collection<TimeHistogram> timeHistogramList = agentHistogram.getTimeHistogram();
                LinkCallDataMap linkCallDataMap = emulationLinkData.getLinkCallDataMap();

                if (logger.isDebugEnabled()) {
                    logger.debug("emulationLink BEFORE:{}", beforeLinkCallData);
                    logger.debug("emulationLink agent:{}", agentHistogram);
                    logger.debug("emulationLink link:{}/{} -> {}/{}", agentHistogram.getTarget(), agentHistogram.getTargetServiceType(),
                            beforeLinkCallData.getTarget(), beforeLinkCallData.getTargetServiceType().getCode());
                }

                linkCallDataMap.addCallData(agentHistogram.getTarget(), agentHistogram.getTargetServiceType(),
                        beforeLinkCallData.getTarget(), beforeLinkCallData.getTargetServiceType(), timeHistogramList);
            }

        }


    }


    private List<LinkData> findEmulationLinkData(LinkDataDuplexMap linkDataDuplexMap) {
        // LinkDataDuplexMap already has a copy of the data - modifying emulationLinkMarker's data has no effect.
        // We must get the data from LinkDataDuplexMap again.
        List<LinkData> searchList = new ArrayList<>();
        for (LinkData emulationLinkData : this.emulationLinkMarker) {
            LinkKey search = getLinkKey(emulationLinkData);
            for (LinkData linkData : linkDataDuplexMap.getSourceLinkDataList()) {
                LinkKey linkKey = getLinkKey(linkData);
                if (linkKey.equals(search)) {
                    searchList.add(linkData);
                }
            }
        }
        return searchList;
    }

    private LinkKey getLinkKey(LinkData emulationLinkData) {
        Application fromApplication = emulationLinkData.getFromApplication();
        Application toApplication = emulationLinkData.getToApplication();
        return new LinkKey(fromApplication, toApplication);
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

        if (!emulationLinkMarker.isEmpty()) {
            logger.debug("Link emulation size:{}", emulationLinkMarker.size());
            // special case
            checkUnsearchEmulationCalleeNode(linkDataDuplexMap, range);
            fillEmulationLink(linkDataDuplexMap, range);
        }

        return linkDataDuplexMap;
    }


    private void checkUnsearchEmulationCalleeNode(LinkDataDuplexMap searchResult, Range range) {

        List<Application> unvisitedList = getUnvisitedEmulationNode();
        if (unvisitedList.isEmpty()) {
            logger.debug("unvisited callee node not found");
            return;
        }

        logger.info("unvisited callee node {}", unvisitedList);

        final LinkDataMap calleeLinkData = new LinkDataMap();
        for (Application application : unvisitedList) {
            LinkDataMap callee = mapStatisticsCalleeDao.selectCallee(application, range);
            logger.debug("calleeNode:{}", callee);
            calleeLinkData.addLinkDataMap(callee);
        }

        LinkDataMap unvisitedNodeFilter = new LinkDataMap();
        for (LinkData linkData : calleeLinkData.getLinkDataList()) {
            Application fromApplication = linkData.getFromApplication();
            if (!fromApplication.getServiceType().isWas()) {
                continue;
            }
            Application emulatedApplication = linkData.getToApplication();
            boolean unvisitedNode = isUnVisitedNode(unvisitedList, emulatedApplication, fromApplication);
            if (unvisitedNode) {
                logger.debug("EmulationCalleeNode:{}", linkData);
                unvisitedNodeFilter.addLinkData(linkData);
            }
        }
        logger.debug("UnVisitedNode:{}", unvisitedNodeFilter);

        for (LinkData linkData : unvisitedNodeFilter.getLinkDataList()) {
            searchResult.addTargetLinkData(linkData);
        }

    }

    private boolean isUnVisitedNode(List<Application> unvisitedList, Application toApplication, Application fromApplication) {
        for (Application unvisitedApplication : unvisitedList) {
            if (toApplication.equals(unvisitedApplication) && linkVisitChecker.isVisitedCaller(fromApplication)) {
                return true;
            }
        }
        return false;
    }

    private List<Application> getUnvisitedEmulationNode() {
        Set<Application> unvisitedList = new HashSet<>();
        for (LinkData linkData : this.emulationLinkMarker) {
            Application toApplication = linkData.getToApplication();
            boolean isVisited = this.linkVisitChecker.isVisitedCaller(toApplication);
            if (!isVisited) {
                unvisitedList.add(toApplication);
            }
        }
        return new ArrayList<>(unvisitedList);
    }


    private LinkDataMap replaceRpcCaller(LinkDataMap caller, Range range) {
        final LinkDataMap replaceRpcCaller = new LinkDataMap();
        for (LinkData callerLink : caller.getLinkDataList()) {
            final List<LinkData> checkedLink = checkRpcCallAccepted(callerLink, range);
            for (LinkData linkData : checkedLink) {
                replaceRpcCaller.addLinkData(linkData);
            }
        }
        return replaceRpcCaller;
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
