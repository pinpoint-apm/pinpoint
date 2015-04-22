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
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.service.map.AcceptApplicationLocalCache;
import com.navercorp.pinpoint.web.service.map.AcceptApplicationLocalCacheV1;
import com.navercorp.pinpoint.web.service.map.RpcApplication;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

import java.util.*;

/**
 * @author emeroad
 */
public class LinkDataSelector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkVisitChecker linkVisitChecker = new LinkVisitChecker();

    private final MapStatisticsCalleeDao mapStatisticsCalleeDao;

    private final MapStatisticsCallerDao mapStatisticsCallerDao;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final AcceptApplicationLocalCache acceptApplicationLocalCache = new AcceptApplicationLocalCache();

    @Deprecated
    private final AcceptApplicationLocalCacheV1 acceptApplicationLocalCacheV1 = new AcceptApplicationLocalCacheV1();

    private final Set<LinkData> emulationLinkMarker = new HashSet<LinkData>();

    public LinkDataSelector(MapStatisticsCalleeDao mapStatisticsCalleeDao, MapStatisticsCallerDao mapStatisticsCallerDao, HostApplicationMapDao hostApplicationMapDao) {
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
    }

    /**
     * Queries for all applications(callee) called by the callerApplication
     *
     * @param callerApplication
     * @param range
     * @return
     */
    private LinkDataDuplexMap selectCaller(Application callerApplication, Range range) {
        // skip if the callerApplication has already been checked
        if (linkVisitChecker.visitCaller(callerApplication)) {
            return new LinkDataDuplexMap();
        }

        LinkDataMap caller = mapStatisticsCallerDao.selectCaller(callerApplication, range);
        if (logger.isDebugEnabled()) {
            logger.debug("Found Caller. count={}, caller={}", caller.size(), callerApplication);
        }

        final LinkDataMap replaceRpcCaller = new LinkDataMap();
        for (LinkData link : caller.getLinkDataList()) {
            final List<LinkData> checkedLink = checkRpcCallAccepted(link, range);
            for (LinkData linkData : checkedLink) {
                replaceRpcCaller.addLinkData(linkData);
            }
        }


        final LinkDataDuplexMap resultCaller = new LinkDataDuplexMap();
        for (LinkData link : replaceRpcCaller.getLinkDataList()) {
            resultCaller.addSourceLinkData(link);

            final Application toApplication = link.getToApplication();
            // skip if toApplication is a terminal or an unknown cloud
            if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
                continue;
            }

            logger.debug("     Find subCaller of {}", toApplication);
            LinkDataDuplexMap callerSub = selectCaller(toApplication, range);
            logger.debug("     Found subCaller. count={}, caller={}", callerSub.size(), toApplication);

            resultCaller.addLinkDataDuplexMap(callerSub);

            // find all callers of queried subCallers as well
            for (LinkData eachCaller : callerSub.getSourceLinkDataList()) {
                logger.debug("     Find callee of {}", eachCaller.getFromApplication());
                LinkDataDuplexMap calleeSub = selectCallee(eachCaller.getFromApplication(), range);
                logger.debug("     Found subCallee. count={}, callee={}", calleeSub.size(), eachCaller.getFromApplication());
                resultCaller.addLinkDataDuplexMap(calleeSub);
            }
        }
        return resultCaller;
    }

    /**
     * Queries for all applications(caller) that called calleeApplication
     *
     * @param calleeApplication
     * @param range
     * @return
     */
    private LinkDataDuplexMap selectCallee(Application calleeApplication, Range range) {
        // skip if the calleeApplication has already been checked
        if (linkVisitChecker.visitCallee(calleeApplication)) {
            return new LinkDataDuplexMap();
        }

        final LinkDataMap callee = mapStatisticsCalleeDao.selectCallee(calleeApplication, range);
        logger.debug("Found Callee. count={}, callee={}", callee.size(), calleeApplication);

        final LinkDataDuplexMap calleeSet = new LinkDataDuplexMap();
        for (LinkData stat : callee.getLinkDataList()) {
            calleeSet.addTargetLinkData(stat);

            // need to find the applications that called me
            LinkDataDuplexMap calleeSub = selectCallee(stat.getFromApplication(), range);
            calleeSet.addLinkDataDuplexMap(calleeSub);

            // find all callees of queried subCallees as well
            for (LinkData eachCallee : calleeSub.getTargetLinkDataList()) {
                // skip if terminal node
                final Application eachCalleeToApplication = eachCallee.getToApplication();
                if (eachCalleeToApplication.getServiceType().isTerminal() || eachCalleeToApplication.getServiceType().isUnknown()) {
                    continue;
                }
                LinkDataDuplexMap callerSub = selectCaller(eachCalleeToApplication, range);
                calleeSet.addLinkDataDuplexMap(callerSub);
            }
        }

        return calleeSet;
    }


    private List<LinkData> checkRpcCallAccepted(LinkData linkData, Range range) {
        // replace if the rpc client's destination has an agent installed and thus has an application name
        final Application toApplication = linkData.getToApplication();
        if (!toApplication.getServiceType().isRpcClient()) {
            return Arrays.asList(linkData);
        }

        logger.debug("checkRpcCallAccepted(). Find applicationName:{} {}", toApplication, range);

        final Set<AcceptApplication> acceptApplicationList = findAcceptApplication(linkData.getFromApplication(), toApplication.getName(), range);
        logger.debug("find accept application:{}", acceptApplicationList);
        if (CollectionUtils.isNotEmpty(acceptApplicationList)) {
            if (acceptApplicationList.size() == 1) {
                logger.debug("Application info replaced. {} => {}", linkData, acceptApplicationList);

                AcceptApplication first = acceptApplicationList.iterator().next();
                final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), first.getApplication(), linkData.getLinkCallDataMap());
                return Arrays.asList(acceptedLinkData);
            } else {
                // special case - there are more than 2 nodes grouped by a single url
                return createVirtualLinkData(linkData, toApplication, acceptApplicationList);
            }
        } else {
            final Application unknown = new Application(toApplication.getName(), ServiceType.UNKNOWN);
            final LinkData unknownLinkData = new LinkData(linkData.getFromApplication(), unknown, linkData.getLinkCallDataMap());
            return Arrays.asList(unknownLinkData);
        }

    }

    private List<LinkData> createVirtualLinkData(LinkData linkData, Application toApplication, Set<AcceptApplication> acceptApplicationList) {
        logger.warn("ono to N replaced. node:{}->host:{} accept:{}", linkData.getFromApplication(), toApplication.getName(), acceptApplicationList);

        List<LinkData> emulationLink = new ArrayList<LinkData>();
        for (AcceptApplication acceptApplication : acceptApplicationList) {
            // linkCallData needs to be modified - remove callHistogram on purpose
            final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), acceptApplication.getApplication(), linkData.getLinkCallDataMap());
            emulationLink.add(acceptedLinkData);
            traceEmulationLink(acceptedLinkData);
        }
        return emulationLink;
    }

    private void traceEmulationLink(LinkData acceptApplication) {
        final boolean add = emulationLinkMarker.add(acceptApplication);
        if (!add) {
            logger.warn("emulationLink add error. {}", acceptApplication );
        }
    }


    private Set<AcceptApplication> findAcceptApplication(Application fromApplication, String host, Range range) {
        logger.debug("findAcceptApplication {} {}", fromApplication, host);
        Set<AcceptApplication> acceptApplicationVer2;
        try {
            // queries twice for backward compatibility - queries for the more recent version first
            // FIXME Remove compatibility code after 6 monthes (from 2014.07)
            acceptApplicationVer2 = findAcceptApplicationVer2(fromApplication, host, range);
            logger.debug("findAcceptApplication2 {}->{} result:{}", fromApplication, host, acceptApplicationVer2);
        } catch (HbaseSystemException ex) {
            acceptApplicationVer2 = Collections.emptySet();
        }

        if (CollectionUtils.isEmpty(acceptApplicationVer2)) {
            Set<AcceptApplication> acceptApplicationVer1 = findAcceptApplicationVer1(host, range);
            if (CollectionUtils.isNotEmpty(acceptApplicationVer1)) {
                logger.debug("acceptApplicationVer1 result:{}", acceptApplicationVer1);
            }
            return acceptApplicationVer1;
        }
        return acceptApplicationVer2;
    }

    private Set<AcceptApplication> findAcceptApplicationVer1(String host, Range range) {

        final Set<AcceptApplication> hit = acceptApplicationLocalCacheV1.get(host);
        if (CollectionUtils.isNotEmpty(hit)) {
            logger.debug("acceptApplicationLocalCacheV1 hit");
            return hit;
        }

        final Set<AcceptApplication> acceptApplicationSet= hostApplicationMapDao.findAcceptApplicationName(host, range);
        this.acceptApplicationLocalCacheV1.put(host, acceptApplicationSet);
        return acceptApplicationLocalCacheV1.get(host);
    }

    private Set<AcceptApplication> findAcceptApplicationVer2(Application fromApplication, String host, Range range) {

        final RpcApplication rpcApplication = new RpcApplication(host, fromApplication);
        final Set<AcceptApplication> hit = this.acceptApplicationLocalCache.get(rpcApplication);
        if (CollectionUtils.isNotEmpty(hit)) {
            logger.debug("acceptApplicationLocalCacheV2 hit");
            return hit;
        }
        final Set<AcceptApplication> acceptApplicationSet= hostApplicationMapDao.findAcceptApplicationName(fromApplication, range);
        this.acceptApplicationLocalCache.put(rpcApplication, acceptApplicationSet);
        return this.acceptApplicationLocalCache.get(rpcApplication);
    }

    private void fillEmulationLink(LinkDataDuplexMap linkDataDuplexMap) {
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
                // There has been a case where targetLinkData was null, but exact event could not be captured for analysis.
                // Logging the case for further analysis should it happen again in the future.
                logger.error("targetLinkData not found findLinkKey:{}", findLinkKey);
                continue;
            }

            // create reversed link data - convert data accepted by the target to target's call data
            LinkCallDataMap targetList = targetLinkData.getLinkCallDataMap();
            Collection<LinkCallData> beforeLinkDataList = beforeImage.getLinkDataList();

            LinkCallData beforeLinkCallData = beforeLinkDataList.iterator().next();
            for (LinkCallData agentHistogram : targetList.getLinkDataList()) {
                Collection<TimeHistogram> timeHistogramList = agentHistogram.getTimeHistogram();
                LinkCallDataMap linkCallDataMap = emulationLinkData.getLinkCallDataMap();

                if (logger.isDebugEnabled()) {
                    logger.debug("emulationLink before:{}", beforeLinkCallData);
                    logger.debug("emulationLink agent:{}", agentHistogram);
                    logger.debug("emulationLink link:{}/{} -> {}/{}", agentHistogram.getTarget(), agentHistogram.getTargetServiceType(),
                            beforeLinkCallData.getTarget(), beforeLinkCallData.getTargetServiceType().getCode());
                }

//                linkCallDataMap.addCallData(beforeLinkCallData.getSource(), beforeLinkCallData.getSourceServiceType().getCode(),
//                        beforeLinkCallData.getTarget(), beforeLinkCallData.getTargetServiceType().getCode(), timeHistogramList);
                linkCallDataMap.addCallData(agentHistogram.getTarget(), agentHistogram.getTargetServiceType(),
                        beforeLinkCallData.getTarget(), beforeLinkCallData.getTargetServiceType(), timeHistogramList);
            }

        }


    }

    public LinkCallData findBeforeAgent(Collection<LinkCallData> linkCallDataCollection, Application target) {
        for (LinkCallData linkCallData : linkCallDataCollection) {
            Application source = new Application(linkCallData.getSource(), linkCallData.getSourceServiceType());
            if (source.equals(target)) {
                logger.debug("findBeforeAgent:{}", linkCallData);
                return linkCallData;
            }
        }
        return null;
    }

    private List<LinkData> findEmulationLinkData(LinkDataDuplexMap linkDataDuplexMap) {
        // LinkDataDuplexMap already has a copy of the data - modifying emulationLinkMarker's data has no effect.
        // We must get the data from LinkDataDuplexMap again.
        List<LinkData> searchList = new ArrayList<LinkData>();
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

    public LinkDataDuplexMap select(Application sourceApplication, Range range) {
        LinkDataDuplexMap caller = selectCaller(sourceApplication, range);
        logger.debug("Result of finding caller {}", caller);

        LinkDataDuplexMap callee = selectCallee(sourceApplication, range);
        logger.debug("Result of finding callee {}", callee);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        linkDataDuplexMap.addLinkDataDuplexMap(caller);
        linkDataDuplexMap.addLinkDataDuplexMap(callee);
        fillEmulationLink(linkDataDuplexMap);

        return linkDataDuplexMap;
    }
}
