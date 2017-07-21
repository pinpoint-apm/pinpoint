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

package com.navercorp.pinpoint.web.service.map.processor;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.service.map.VirtualLinkMarker;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Replaces link data pointing to domains into applications if the target has an agent installed.
 *
 * @author HyunGil Jeong
 */
public class RpcCallProcessor implements LinkDataMapProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HostApplicationMapDao hostApplicationMapDao;

    private final VirtualLinkMarker virtualLinkMarker;

    private final Map<AcceptApplicationCacheKey, Set<AcceptApplication>> acceptApplicationCache = Maps.newConcurrentMap();

    private final AcceptApplicationLocalCache rpcAcceptApplicationCache = new AcceptApplicationLocalCache();

    public RpcCallProcessor(HostApplicationMapDao hostApplicationMapDao, VirtualLinkMarker virtualLinkMarker) {
        if (hostApplicationMapDao == null) {
            throw new NullPointerException("hostApplicationMapDao must not be null");
        }
        if (virtualLinkMarker == null) {
            throw new NullPointerException("virtualLinkMarker must not be null");
        }
        this.hostApplicationMapDao = hostApplicationMapDao;
        this.virtualLinkMarker = virtualLinkMarker;
    }

    @Override
    public LinkDataMap processLinkDataMap(LinkDataMap linkDataMap, Range range) {
        final LinkDataMap replacedLinkDataMap = new LinkDataMap();
        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final List<LinkData> replacedLinkDatas = replaceLinkData(linkData, range);
            for (LinkData replacedLinkData : replacedLinkDatas)
                replacedLinkDataMap.addLinkData(replacedLinkData);
        }
        return replacedLinkDataMap;
    }

    private List<LinkData> replaceLinkData(LinkData linkData, Range range) {
        final Application toApplication = linkData.getToApplication();
        if (toApplication.getServiceType().isRpcClient() || toApplication.getServiceType().isQueue()) {
            // rpc client's destination could have an agent installed in which case the link data must be replaced to point
            // to the destination application.
            logger.debug("Finding accept applications for {}, {}", toApplication, range);
            final Set<AcceptApplication> acceptApplicationList = findAcceptApplications(linkData.getFromApplication(), toApplication.getName(), range);
            logger.debug("Found accept applications: {}", acceptApplicationList);
            if (!CollectionUtils.isEmpty(acceptApplicationList)) {
                if (acceptApplicationList.size() == 1) {
                    logger.debug("Application info replaced. {} => {}", linkData, acceptApplicationList);

                    AcceptApplication first = acceptApplicationList.iterator().next();
                    final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), first.getApplication());
                    acceptedLinkData.setLinkCallDataMap(linkData.getLinkCallDataMap());
                    return Collections.singletonList(acceptedLinkData);
                } else {
                    // special case - there are more than 2 nodes grouped by a single url
                    return virtualLinkMarker.createVirtualLinkData(linkData, toApplication, acceptApplicationList);
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
        return Collections.singletonList(linkData);
    }

    private Set<AcceptApplication> findAcceptApplications(Application fromApplication, String host, Range range) {
        logger.debug("findAcceptApplication {} {}", fromApplication, host);

        final RpcApplication rpcApplication = new RpcApplication(host, fromApplication);
        final Set<AcceptApplication> hit = this.rpcAcceptApplicationCache.get(rpcApplication);
        if (!CollectionUtils.isEmpty(hit)) {
            logger.debug("rpcAcceptApplicationCache hit {}", rpcApplication);
            return hit;
        }
        final Set<AcceptApplication> acceptApplicationSet = getAcceptApplications(fromApplication, range);
        this.rpcAcceptApplicationCache.put(rpcApplication, acceptApplicationSet);

        Set<AcceptApplication> acceptApplication = this.rpcAcceptApplicationCache.get(rpcApplication);
        logger.debug("findAcceptApplication {}->{} result:{}", fromApplication, host, acceptApplication);
        return acceptApplication;
    }

    private Set<AcceptApplication> getAcceptApplications(Application fromApplication, Range range) {
        AcceptApplicationCacheKey cacheKey = new AcceptApplicationCacheKey(fromApplication, range);
        Set<AcceptApplication> cachedAcceptApplications = acceptApplicationCache.get(cacheKey);
        if (cachedAcceptApplications == null) {
            logger.debug("acceptApplicationCache hit {}", fromApplication);
            Set<AcceptApplication> queriedAcceptApplications = hostApplicationMapDao.findAcceptApplicationName(fromApplication, range);
            Set<AcceptApplication> acceptApplications = Sets.newConcurrentHashSet();
            if (!CollectionUtils.isEmpty(queriedAcceptApplications)) {
                acceptApplications.addAll(queriedAcceptApplications);
            }
            cachedAcceptApplications = acceptApplicationCache.putIfAbsent(cacheKey, acceptApplications);
            if (cachedAcceptApplications == null) {
                cachedAcceptApplications = acceptApplications;
            }
        } else {
            logger.debug("acceptApplicationCache hit {}", fromApplication);
        }
        return cachedAcceptApplications;
    }

    private static class AcceptApplicationCacheKey {
        private final Application application;
        private final Range range;

        private AcceptApplicationCacheKey(Application application, Range range) {
            this.application = application;
            this.range = range;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AcceptApplicationCacheKey cacheKey = (AcceptApplicationCacheKey) o;

            if (application != null ? !application.equals(cacheKey.application) : cacheKey.application != null)
                return false;
            return range != null ? range.equals(cacheKey.range) : cacheKey.range == null;
        }

        @Override
        public int hashCode() {
            int result = application != null ? application.hashCode() : 0;
            result = 31 * result + (range != null ? range.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CacheKey{");
            sb.append("application=").append(application);
            sb.append(", range=").append(range);
            sb.append('}');
            return sb.toString();
        }
    }
}
