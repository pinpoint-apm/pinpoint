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

import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessors;
import com.navercorp.pinpoint.web.applicationmap.map.processor.RpcCallProcessor;
import com.navercorp.pinpoint.web.applicationmap.service.LinkDataMapService;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * @author HyunGil Jeong
 */
@Component
public class LinkSelectorFactory {

    private final LinkDataMapService linkDataMapService;

    private final ApplicationsMapCreatorFactory applicationsMapCreatorFactory;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final ServerMapDataFilter serverMapDataFilter;

    public LinkSelectorFactory(
            LinkDataMapService linkDataMapService,
            ApplicationsMapCreatorFactory applicationsMapCreatorFactory,
            HostApplicationMapDao hostApplicationMapDao,
            Optional<ServerMapDataFilter> serverMapDataFilter) {
        this.linkDataMapService = Objects.requireNonNull(linkDataMapService, "linkDataMapService");
        this.applicationsMapCreatorFactory = Objects.requireNonNull(applicationsMapCreatorFactory, "applicationsMapCreatorFactory");
        this.hostApplicationMapDao = Objects.requireNonNull(hostApplicationMapDao, "hostApplicationMapDao");
        this.serverMapDataFilter = Objects.requireNonNull(serverMapDataFilter, "serverMapDataFilter").orElse(null);
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
