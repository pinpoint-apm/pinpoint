/*
 * Copyright 2025 NAVER Corp.
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
 */

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.applicationmap.map.VirtualLinkMarker;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Replaces link data pointing to domains into applications if the target has an agent installed.
 *
 * @author HyunGil Jeong
 */
public class RpcCallProcessor implements LinkDataMapProcessor {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HostApplicationMapDao hostApplicationMapDao;

    private final VirtualLinkMarker virtualLinkMarker;

    private final ConcurrentMap<AcceptApplicationCacheKey, AcceptApplicationSet> acceptApplicationCache = new ConcurrentHashMap<>();

    private final AcceptApplicationLocalCache rpcAcceptApplicationCache = new AcceptApplicationLocalCache();

    public RpcCallProcessor(HostApplicationMapDao hostApplicationMapDao, VirtualLinkMarker virtualLinkMarker) {
        this.hostApplicationMapDao = Objects.requireNonNull(hostApplicationMapDao, "hostApplicationMapDao");
        this.virtualLinkMarker = Objects.requireNonNull(virtualLinkMarker, "virtualLinkMarker");
    }

    @Override
    public LinkDataMap processLinkDataMap(LinkDirection direction, LinkDataMap linkDataMap, TimeWindow timeWindow) {
        final Range range = timeWindow.getWindowRange();
        final LinkDataMap replacedLinkDataMap = new LinkDataMap();
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final List<LinkData> replacedLinkDatas = replaceLinkData(direction, linkData, range);
            for (LinkData replacedLinkData : replacedLinkDatas)
                replacedLinkDataMap.addLinkData(replacedLinkData);
        }
        return replacedLinkDataMap;
    }

    private List<LinkData> replaceLinkData(LinkDirection direction, LinkData linkData, Range range) {
        final Application toApplication = linkData.getToApplication();
        if (toApplication.getServiceType().isRpcClient() || toApplication.getServiceType().isQueue()) {
            // rpc client's destination could have an agent installed in which case the link data must be replaced to point
            // to the destination application.
            logger.debug("Finding {} accept applications for {}, {}", direction, toApplication, range);
            final AcceptApplicationSet acceptApplicationList = findAcceptApplications(linkData.getFromApplication(), toApplication.getName(), range);
            logger.debug("Found {} accept applications: {}", direction, acceptApplicationList);
            final int size = AcceptApplicationSet.size(acceptApplicationList);
            if (size > 0) {
                if (size == 1) {
                    logger.debug("Application info replaced. {} {} => {}", direction, linkData, acceptApplicationList);

                    AcceptApplication first = acceptApplicationList.firstElement();
                    final LinkData acceptedLinkData = LinkData.copyOf(linkData.getFromApplication(), first.getApplication(), linkData.getLinkCallDataMap());
                    return Collections.singletonList(acceptedLinkData);
                } else {
                    // special case - there are more than 2 nodes grouped by a single url
                    return virtualLinkMarker.createVirtualLink(linkData, toApplication, acceptApplicationList);
                }
            } else {
                // for queues, accept application may not exist if no consumers have an agent installed
                if (toApplication.getServiceType().isQueue()) {
                    return Collections.singletonList(linkData);
                } else {
                    final Application unknown = new Application(toApplication.getName(), ServiceType.UNKNOWN);
                    final LinkData unknownLinkData = LinkData.copyOf(linkData.getFromApplication(), unknown, linkData.getLinkCallDataMap());
                    return Collections.singletonList(unknownLinkData);
                }
            }
        }
        return Collections.singletonList(linkData);
    }

    private AcceptApplicationSet filterAlias(Set<AcceptApplication> acceptApplicationList) {
        final int size = acceptApplicationList.size();
        if (size == 0) {
            return new AcceptApplicationSet();
        }
        if (size < 2) {
            return AcceptApplicationSet.copyOf(acceptApplicationList);
        }

        final AcceptApplicationSet resultSet = new AcceptApplicationSet();
        for (AcceptApplication acceptApplication : acceptApplicationList) {
            if (!acceptApplication.getApplication().getServiceType().isAlias()) {
                resultSet.add(acceptApplication);
            } else {
                logger.debug("deduct alias application {}", acceptApplication);
            }
        }
        return resultSet;
    }

    @Nullable
    private AcceptApplicationSet findAcceptApplications(Application fromApplication, String host, Range range) {
        logger.debug("findAcceptApplication {} {}", fromApplication, host);

        final RpcApplication rpcApplication = new RpcApplication(host, fromApplication);
        final AcceptApplicationSet hit = this.rpcAcceptApplicationCache.get(rpcApplication);
        if (AcceptApplicationSet.hasSize(hit)) {
            logger.debug("rpcAcceptApplicationCache hit {}", rpcApplication);
            return hit;
        }
        final AcceptApplicationSet acceptApplicationSet = getAcceptApplications(fromApplication, range);

        this.rpcAcceptApplicationCache.put(rpcApplication, acceptApplicationSet);
        AcceptApplicationSet acceptApplication = this.rpcAcceptApplicationCache.get(rpcApplication);
        if (logger.isDebugEnabled()) {
            logger.debug("findAcceptApplication {}->{} result:{}", fromApplication, host, acceptApplication);
        }
        return acceptApplication;
    }

    private AcceptApplicationSet getAcceptApplications(Application fromApplication, Range range) {
        AcceptApplicationCacheKey cacheKey = new AcceptApplicationCacheKey(fromApplication, range);
        final AcceptApplicationSet cachedAcceptApplications = acceptApplicationCache.get(cacheKey);
        if (cachedAcceptApplications != null) {
            logger.debug("acceptApplicationCache hit {}", fromApplication);
            return cachedAcceptApplications;
        }
        AcceptApplicationSet acceptApplications = getAcceptApplicationsFromHostApplication(fromApplication, range);
        return acceptApplicationCache.computeIfAbsent(cacheKey, acceptApplicationCacheKey -> acceptApplications);
    }

    @NotNull
    private AcceptApplicationSet getAcceptApplicationsFromHostApplication(Application fromApplication, Range range) {
        logger.debug("acceptApplicationCache miss {}", fromApplication);
        Set<AcceptApplication> queriedAcceptApplications = hostApplicationMapDao.findAcceptApplicationName(fromApplication, range);

        final AcceptApplicationSet filteredApplicationList = filterAlias(queriedAcceptApplications);
        logger.debug("filteredApplicationList: {}", filteredApplicationList);

        return filteredApplicationList;
    }

    private record AcceptApplicationCacheKey(Application application, Range range) {
    }
}
