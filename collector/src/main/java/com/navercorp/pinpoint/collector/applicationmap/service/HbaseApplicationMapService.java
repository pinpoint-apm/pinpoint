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

package com.navercorp.pinpoint.collector.applicationmap.service;

import com.navercorp.pinpoint.collector.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.applicationmap.model.AcceptorHostRow;
import com.navercorp.pinpoint.collector.applicationmap.model.ApplicationMapBuilder;
import com.navercorp.pinpoint.collector.applicationmap.model.ApplicationMapModel;
import com.navercorp.pinpoint.collector.applicationmap.model.InLinkRow;
import com.navercorp.pinpoint.collector.applicationmap.model.OutLinkRow;
import com.navercorp.pinpoint.collector.applicationmap.model.ResponseTimeRow;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Trace service implementation for HBase storage.
 * Builds an {@link ApplicationMapModel} from the span first, then stores the model.
 */
@Service
public class HbaseApplicationMapService implements ApplicationMapService {

    private final Logger logger = LogManager.getLogger(getClass());
    private final HostApplicationMapDao hostApplicationMapDao;

    private final LinkService linkService;

    private final ApplicationMapBuilder applicationMapBuilder;

    public HbaseApplicationMapService(HostApplicationMapDao hostApplicationMapDao,
                                      LinkService linkService,
                                      ServiceTypeRegistryService registry) {
        this.hostApplicationMapDao = Objects.requireNonNull(hostApplicationMapDao, "hostApplicationMapDao");
        this.linkService = Objects.requireNonNull(linkService, "linkService");
        Objects.requireNonNull(registry, "registry");
        this.applicationMapBuilder = new ApplicationMapBuilder(registry);
    }

    @Override
    public void insertSpanChunk(final SpanChunkBo spanChunkBo) {
        final ApplicationMapModel model = applicationMapBuilder.build(spanChunkBo);
        write(model);
    }

    @Override
    public void insertSpan(final SpanBo spanBo) {
        final ApplicationMapModel model = applicationMapBuilder.build(spanBo);
        write(model);
    }

    private void write(ApplicationMapModel model) {
        if (logger.isDebugEnabled()) {
            logger.debug("MapModel {}", model.dump());
        }

        final long requestTime = model.getRequestTime();

        for (AcceptorHostRow row : model.getAcceptorHosts()) {
            hostApplicationMapDao.insert(requestTime, row.parentVertex(), row.vertex(), row.host());
        }
        for (OutLinkRow row : model.getOutLinks()) {
            linkService.updateOutLink(requestTime, row.selfVertex(),
                    row.outVertex(), row.outHost(), row.elapsed(), row.error());
        }
        for (InLinkRow row : model.getInLinks()) {
            linkService.updateInLink(requestTime, row.inVertex(),
                    row.selfVertex(), row.selfHost(), row.elapsed(), row.error());
        }
        for (ResponseTimeRow row : model.getResponseTimes()) {
            linkService.updateResponseTime(requestTime, row.selfVertex(), row.agentId(), row.elapsed(), row.error());
        }
    }
}
