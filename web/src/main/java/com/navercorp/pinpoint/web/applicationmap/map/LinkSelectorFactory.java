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
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 */
@Component
public class LinkSelectorFactory {

    private final LinkDataMapService linkDataMapService;

    private final ApplicationsMapCreatorFactory applicationsMapCreatorFactory;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final ServerMapDataFilter serverMapDataFilter;
    private final Supplier<LinkDataMapProcessor> applicationLimiterProcessorFactory;

    public LinkSelectorFactory(
            LinkDataMapService linkDataMapService,
            ApplicationsMapCreatorFactory applicationsMapCreatorFactory,
            HostApplicationMapDao hostApplicationMapDao,
            Optional<ServerMapDataFilter> serverMapDataFilter,
            Supplier<LinkDataMapProcessor> applicationLimiterProcessorFactory) {
        this.linkDataMapService = Objects.requireNonNull(linkDataMapService, "linkDataMapService");
        this.applicationsMapCreatorFactory = Objects.requireNonNull(applicationsMapCreatorFactory, "applicationsMapCreatorFactory");
        this.hostApplicationMapDao = Objects.requireNonNull(hostApplicationMapDao, "hostApplicationMapDao");
        this.serverMapDataFilter = Objects.requireNonNull(serverMapDataFilter, "serverMapDataFilter").orElse(null);
        this.applicationLimiterProcessorFactory = Objects.requireNonNull(applicationLimiterProcessorFactory, "applicationLimiterProcessorFactory");
    }

    public LinkSelector createLinkSelector(LinkSelectorType linkSelectorType) {
        return createLinkSelector(linkSelectorType, LinkDataMapProcessor.NO_OP, LinkDataMapProcessor.NO_OP);
    }

    public LinkSelector createLinkSelector(LinkSelectorType linkSelectorType, LinkDataMapProcessor outLinkProcessor, LinkDataMapProcessor inLinkProcessor) {

        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        VirtualLinkHandler virtualLinkHandler = new VirtualLinkHandler(linkDataMapService, virtualLinkMarker);


        LinkDataMapProcessors outLinkProcessors = newOutLinkProcessors(outLinkProcessor, virtualLinkMarker);

        LinkDataMapProcessors inLinkProcessors = newInLinkProcessor(inLinkProcessor);

        ApplicationMapCreator applicationMapCreator = new DefaultApplicationMapCreator(linkDataMapService, outLinkProcessors, inLinkProcessors);

        ApplicationsMapCreator applicationsMapCreator = applicationsMapCreatorFactory.create(applicationMapCreator);

        if (LinkSelectorType.UNIDIRECTIONAL == linkSelectorType) {
            return new UnidirectionalLinkSelector(applicationsMapCreator, virtualLinkHandler, serverMapDataFilter);
        } else {
            return new BidirectionalLinkSelector(applicationsMapCreator, virtualLinkHandler, serverMapDataFilter);
        }
    }

    private LinkDataMapProcessors newInLinkProcessor(LinkDataMapProcessor inLinkDataMapProcessor) {
        LinkDataMapProcessors.Builder inLinkBuilder = LinkDataMapProcessors.newBuilder();
        inLinkBuilder.addLinkProcessor(this.applicationLimiterProcessorFactory.get());
        inLinkBuilder.addLinkProcessor(inLinkDataMapProcessor);
        return inLinkBuilder.build();
    }

    private LinkDataMapProcessors newOutLinkProcessors(LinkDataMapProcessor outLinkDataMapProcessor, VirtualLinkMarker virtualLinkMarker) {
        LinkDataMapProcessors.Builder outLinkBuilder = LinkDataMapProcessors.newBuilder();
        outLinkBuilder.addLinkProcessor(this.applicationLimiterProcessorFactory.get());
        outLinkBuilder.addLinkProcessor(new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker));
        outLinkBuilder.addLinkProcessor(outLinkDataMapProcessor);
        return outLinkBuilder.build();
    }
}
