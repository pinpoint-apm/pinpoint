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

import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.LinkDataMapService;
import com.navercorp.pinpoint.web.service.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.service.map.processor.LinkDataMapProcessors;
import com.navercorp.pinpoint.web.service.map.processor.RpcCallProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class LinkSelectorFactory {

    private final LinkDataMapService linkDataMapService;

    private final ApplicationsMapCreatorFactory applicationsMapCreatorFactory;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final ServerMapDataFilter serverMapDataFilter;

    @Autowired(required = false)
    public LinkSelectorFactory(
            LinkDataMapService linkDataMapService,
            ApplicationsMapCreatorFactory appliationsMapCreatorFactory,
            HostApplicationMapDao hostApplicationMapDao) {
        this(linkDataMapService, appliationsMapCreatorFactory, hostApplicationMapDao, null);
    }

    @Autowired(required = false)
    public LinkSelectorFactory(
            LinkDataMapService linkDataMapService,
            ApplicationsMapCreatorFactory appliationsMapCreatorFactory,
            HostApplicationMapDao hostApplicationMapDao,
            ServerMapDataFilter serverMapDataFilter) {
        this.linkDataMapService = linkDataMapService;
        this.applicationsMapCreatorFactory = appliationsMapCreatorFactory;
        this.hostApplicationMapDao = hostApplicationMapDao;
        this.serverMapDataFilter = serverMapDataFilter;
    }

    public LinkSelector createLinkSelector(LinkSelectorType linkSelectorType) {
        return createLinkSelector(linkSelectorType, LinkDataMapProcessor.NO_OP, LinkDataMapProcessor.NO_OP);
    }

    public LinkSelector createLinkSelector(LinkSelectorType linkSelectorType, LinkDataMapProcessor callerLinkDataMapProcessor, LinkDataMapProcessor calleeLinkDataMapProcessor) {
        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        VirtualLinkHandler virtualLinkHandler = new VirtualLinkHandler(linkDataMapService, virtualLinkMarker);

        LinkDataMapProcessors callerLinkDataMapProcessors = new LinkDataMapProcessors();
        callerLinkDataMapProcessors.addLinkDataMapProcessor(new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker));
        callerLinkDataMapProcessors.addLinkDataMapProcessor(callerLinkDataMapProcessor);

        LinkDataMapProcessors calleeLinkDataMapProcessors = new LinkDataMapProcessors();
        calleeLinkDataMapProcessors.addLinkDataMapProcessor(calleeLinkDataMapProcessor);

        ApplicationMapCreator applicationMapCreator = new DefaultApplicationMapCreator(linkDataMapService, callerLinkDataMapProcessors, calleeLinkDataMapProcessors);

        ApplicationsMapCreator applicationsMapCreator = applicationsMapCreatorFactory.create(applicationMapCreator);

        if (LinkSelectorType.UNIDIRECTIONAL == linkSelectorType) {
            return new UnidirectionalLinkSelector(applicationsMapCreator, virtualLinkHandler, serverMapDataFilter);
        } else {
            return new BidirectionalLinkSelector(applicationsMapCreator, virtualLinkHandler, serverMapDataFilter);
        }
    }
}
