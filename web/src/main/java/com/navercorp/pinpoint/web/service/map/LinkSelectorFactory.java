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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class LinkSelectorFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String mode;

    private final LinkDataMapService linkDataMapService;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final ServerMapDataFilter serverMapDataFilter;

    @Autowired(required = false)
    public LinkSelectorFactory(
            @Value("#{pinpointWebProps['web.servermap.selector.mode'] ?: 'v1'}") String mode,
            LinkDataMapService linkDataMapService,
            HostApplicationMapDao hostApplicationMapDao) {
        this(mode, linkDataMapService, hostApplicationMapDao, null);
    }

    @Autowired(required = false)
    public LinkSelectorFactory(
            @Value("#{pinpointWebProps['web.servermap.selector.mode'] ?: 'v1'}") String mode,
            LinkDataMapService linkDataMapService,
            HostApplicationMapDao hostApplicationMapDao,
            ServerMapDataFilter serverMapDataFilter) {
        this.mode = mode;
        this.linkDataMapService = linkDataMapService;
        this.hostApplicationMapDao = hostApplicationMapDao;
        this.serverMapDataFilter = serverMapDataFilter;
    }

    public LinkSelector create() {
        logger.info("LinkSelector mode : {}", mode);
        if (mode.equalsIgnoreCase("v2")) {
            VirtualLinkHandler virtualLinkHandler = new VirtualLinkHandler(linkDataMapService);
            LinkDataMapCreator linkDataMapCreator = new DefaultLinkDataMapCreator(linkDataMapService, hostApplicationMapDao, virtualLinkHandler);
            return new BFSLinkSelectorV2(linkDataMapCreator, virtualLinkHandler, serverMapDataFilter);
        } else {
            return new BFSLinkSelector(linkDataMapService, hostApplicationMapDao, serverMapDataFilter);
        }
    }
}
