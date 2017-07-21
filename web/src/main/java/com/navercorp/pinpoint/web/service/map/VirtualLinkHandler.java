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

import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.service.LinkDataMapService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class VirtualLinkHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkDataMapService linkDataMapService;

    private final VirtualLinkMarker virtualLinkMarker;

    public VirtualLinkHandler(LinkDataMapService linkDataMapService, VirtualLinkMarker virtualLinkMarker) {
        if (linkDataMapService == null) {
            throw new NullPointerException("linkDataMapService must not be null");
        }
        if (virtualLinkMarker == null) {
            throw new NullPointerException("virtualLinkMarker must not be null");
        }
        this.linkDataMapService = linkDataMapService;
        this.virtualLinkMarker = virtualLinkMarker;
    }

    public LinkDataDuplexMap processVirtualLinks(LinkDataDuplexMap linkDataDuplexMap, LinkVisitChecker linkVisitChecker, Range range) {
        Set<LinkData> virtualLinkDataSet = virtualLinkMarker.getVirtualLinkData();
        if (virtualLinkDataSet.isEmpty()) {
            return linkDataDuplexMap;
        }
        logger.debug("Virtual link size : {}", virtualLinkDataSet.size());
        List<Application> unpopulatedEmulatedNodes = getUnpopulatedEmulatedNodes(linkDataDuplexMap.getTargetLinkDataMap(), virtualLinkDataSet);
        if (unpopulatedEmulatedNodes.isEmpty()) {
            logger.debug("unpopulated emulated node not found.");
        } else {
            logger.info("unpopulated emulated nodes : {}", unpopulatedEmulatedNodes);
            for (Application unpopulatedEmulatedNode : unpopulatedEmulatedNodes) {
                for (LinkData emulatedNodeCalleeLinkData : getEmulatedNodeCalleeLinkData(linkVisitChecker, unpopulatedEmulatedNode, range)) {
                    linkDataDuplexMap.addTargetLinkData(emulatedNodeCalleeLinkData);
                }
            }
        }

        fillEmulationLink(linkDataDuplexMap, virtualLinkDataSet);
        return linkDataDuplexMap;
    }

    private List<Application> getUnpopulatedEmulatedNodes(LinkDataMap targetLinkDataMap, Set<LinkData> virtualLinkDataSet) {
        Set<Application> unpopulatedEmulatedNodes = new HashSet<>();
        for (LinkData virtualLinkData : virtualLinkDataSet) {
            Application toApplication = virtualLinkData.getToApplication();
            if (targetLinkDataMap.getLinkData(new LinkKey(virtualLinkData.getFromApplication(), toApplication)) == null) {
                unpopulatedEmulatedNodes.add(toApplication);
            }
        }
        return new ArrayList<>(unpopulatedEmulatedNodes);
    }

    private Collection<LinkData> getEmulatedNodeCalleeLinkData(LinkVisitChecker linkVisitChecker, Application emulatedNode, Range range) {
        LinkDataMap calleeLinkDataMap = linkDataMapService.selectCalleeLinkDataMap(emulatedNode, range);
        logger.debug("emulated node [{}] callee LinkDataMap:{}", emulatedNode, calleeLinkDataMap);

        LinkDataMap filteredCalleeLinkDataMap = new LinkDataMap();
        for (LinkData calleeLinkData : calleeLinkDataMap.getLinkDataList()) {
            Application fromApplication = calleeLinkData.getFromApplication();
            // filter callee link data from non-WAS nodes
            if (!fromApplication.getServiceType().isWas()) {
                logger.trace("filtered {} as {} is not a WAS node", calleeLinkData, fromApplication);
                continue;
            }
            // filter callee link data from nodes that haven't been visited as we don't need them
            if (!linkVisitChecker.isVisitedCaller(fromApplication)) {
                logger.trace("filtered {} as {} is not in scope of the current server map", calleeLinkData, fromApplication);
                continue;
            }
            logger.debug("emulated node [{}] callee LinkData:{}", emulatedNode, calleeLinkData);
            filteredCalleeLinkDataMap.addLinkData(calleeLinkData);
        }
        logger.debug("emulated node [{}] filtered callee LinkDataMap:{}", emulatedNode, filteredCalleeLinkDataMap);
        return filteredCalleeLinkDataMap.getLinkDataList();
    }

    private void fillEmulationLink(LinkDataDuplexMap linkDataDuplexMap, Set<LinkData> emulationLinkMarker) {
        // TODO need to be reimplemented - virtual node creation logic needs an overhaul.
        // Currently, only the reversed relationship node is displayed. We need to create a virtual node and convert the rpc data appropriately.
        logger.debug("this.emulationLinkMarker:{}", emulationLinkMarker);
        List<LinkData> emulationLinkDataList = findEmulationLinkData(linkDataDuplexMap, emulationLinkMarker);

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

    private List<LinkData> findEmulationLinkData(LinkDataDuplexMap linkDataDuplexMap, Set<LinkData> emulationLinkMarker) {
        // LinkDataDuplexMap already has a copy of the data - modifying emulationLinkMarker's data has no effect.
        // We must get the data from LinkDataDuplexMap again.
        List<LinkData> searchList = new ArrayList<>();
        for (LinkData emulationLinkData : emulationLinkMarker) {
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
}
