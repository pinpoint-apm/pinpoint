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

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.service.LinkDataMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author HyunGil Jeong
 */
@Component
public class ApplicationsMapCreatorFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String mode;

    private final LinkDataMapService linkDataMapService;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final ExecutorService executorService;

    @Autowired
    public ApplicationsMapCreatorFactory(
            @Value("#{pinpointWebProps['web.servermap.creator.mode'] ?: 'serial'}") String mode,
            @Value("#{pinpointWebProps['web.servermap.creator.parallel.maxthreads'] ?: '16'}") int threadCount,
            HostApplicationMapDao hostApplicationMapDao,
            LinkDataMapService linkDataMapService) {
        logger.info("ApplicationsMapCreatorFactory mode : {}", mode);
        this.mode = mode;
        this.linkDataMapService = linkDataMapService;
        this.hostApplicationMapDao = hostApplicationMapDao;
        if (this.mode.equalsIgnoreCase("parallel")) {
            this.executorService = Executors.newFixedThreadPool(threadCount, new PinpointThreadFactory("Pinpoint-parallel-link-selector", true));
        } else {
            this.executorService = null;
        }
    }

    public ApplicationsMapCreator create(VirtualLinkMarker virtualLinkMarker) {
        RpcCallReplacer rpcCallReplacer = new RpcCallReplacer(hostApplicationMapDao, virtualLinkMarker);
        ApplicationMapCreator applicationMapCreator = new DefaultApplicationMapCreator(linkDataMapService, rpcCallReplacer);
        if (mode.equalsIgnoreCase("parallel")) {
            return new ParallelApplicationsMapCreator(applicationMapCreator, executorService);
        }
        return new SerialApplicationsMapCreator(applicationMapCreator);
    }

}
