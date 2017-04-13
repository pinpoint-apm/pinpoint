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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.service.LinkDataMapService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class DefaultLinkDataMapCreator implements LinkDataMapCreator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkDataMapService linkDataMapService;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final AcceptApplicationLocalCache acceptApplicationLocalCache = new AcceptApplicationLocalCache();

    private final VirtualLinkHandler virtualLinkHandler;

    DefaultLinkDataMapCreator(LinkDataMapService linkDataMapService, HostApplicationMapDao hostApplicationMapDao, VirtualLinkHandler virtualLinkHandler) {
        if (linkDataMapService == null) {
            throw new NullPointerException("linkDataMapService must not be null");
        }
        if (hostApplicationMapDao == null) {
            throw new NullPointerException("hostApplicationMapDao must not be null");
        }
        if (virtualLinkHandler == null) {
            throw new NullPointerException("virtualLinkHandler must not be null");
        }
        this.linkDataMapService = linkDataMapService;
        this.hostApplicationMapDao = hostApplicationMapDao;
        this.virtualLinkHandler = virtualLinkHandler;
    }

    @Override
    public LinkDataMap createCallerLinkDataMap(Application application, Range range) {
        LinkDataMap caller = linkDataMapService.selectCallerLinkDataMap(application, range);
        final LinkDataMap replacedDataMap = new LinkDataMap();
        for (LinkData callerLinkData : caller.getLinkDataList()) {
            final List<LinkData> replacedLinkDatas = replaceLinkData(callerLinkData, range);
            for (LinkData replacedLinkData : replacedLinkDatas) {
                replacedDataMap.addLinkData(replacedLinkData);
            }
        }
        return replacedDataMap;
    }

    @Override
    public LinkDataMap createCalleeLinkDataMap(Application application, Range range) {
        return linkDataMapService.selectCalleeLinkDataMap(application, range);
    }

    private List<LinkData> replaceLinkData(LinkData linkData, Range range) {
        final Application toApplication = linkData.getToApplication();
        if (!toApplication.getServiceType().isRpcClient() && !toApplication.getServiceType().isQueue()) {
            return Collections.singletonList(linkData);
        }

        // rpc client's destination could have an agent installed in which case the link data must be replaced to point
        // to the desctination application.
        logger.debug("Finding accept applications for {}, {}", toApplication, range);
        final Set<AcceptApplication> acceptApplicationList = findAcceptApplications(linkData.getFromApplication(), toApplication.getName(), range);
        logger.debug("Found accept applications: {}", acceptApplicationList);
        if (CollectionUtils.isNotEmpty(acceptApplicationList)) {
            if (acceptApplicationList.size() == 1) {
                logger.debug("Application info replaced. {} => {}", linkData, acceptApplicationList);

                AcceptApplication first = acceptApplicationList.iterator().next();
                final LinkData acceptedLinkData = new LinkData(linkData.getFromApplication(), first.getApplication());
                acceptedLinkData.setLinkCallDataMap(linkData.getLinkCallDataMap());
                return Collections.singletonList(acceptedLinkData);
            } else {
                // special case - there are more than 2 nodes grouped by a single url
                return virtualLinkHandler.createVirtualLinkData(linkData, toApplication, acceptApplicationList);
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

    private Set<AcceptApplication> findAcceptApplications(Application fromApplication, String host, Range range) {
        logger.debug("findAcceptApplication {} {}", fromApplication, host);

        final RpcApplication rpcApplication = new RpcApplication(host, fromApplication);
        final Set<AcceptApplication> hit = this.acceptApplicationLocalCache.get(rpcApplication);
        if (CollectionUtils.isNotEmpty(hit)) {
            logger.debug("acceptApplicationLocalCache hit {}", rpcApplication);
            return hit;
        }
        final Set<AcceptApplication> acceptApplicationSet = hostApplicationMapDao.findAcceptApplicationName(fromApplication, range);
        this.acceptApplicationLocalCache.put(rpcApplication, acceptApplicationSet);

        Set<AcceptApplication> acceptApplication = this.acceptApplicationLocalCache.get(rpcApplication);
        logger.debug("findAcceptApplication {}->{} result:{}", fromApplication, host, acceptApplication);
        return acceptApplication;
    }
}
