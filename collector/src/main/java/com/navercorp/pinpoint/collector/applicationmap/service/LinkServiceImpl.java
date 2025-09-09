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

import com.navercorp.pinpoint.collector.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapResponseTimeDao;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * @author netspider
 * @author jaehong.kim
 */
@Validated
public class LinkServiceImpl implements LinkService {
    private final MapInLinkDao inLinkDao;
    private final MapOutLinkDao outLinkDao;
    private final MapResponseTimeDao responseTimeDao;

    public LinkServiceImpl(MapInLinkDao inLinkDao, MapOutLinkDao outLinkDao, MapResponseTimeDao responseTimeDao) {
        this.inLinkDao = Objects.requireNonNull(inLinkDao, "inLinkDao");
        this.outLinkDao = Objects.requireNonNull(outLinkDao, "outLinkDao");
        this.responseTimeDao = Objects.requireNonNull(responseTimeDao, "responseTimeDao");
    }

    @Override
    public void updateOutLink(
            long requestTime,
            Vertex selfVertex,
            @NotBlank String selfAgentId,
            Vertex outVertex,
            String outHost,
            int elapsed, boolean isError
    ) {
        outLinkDao.outLink(requestTime, selfVertex, selfAgentId,
                outVertex, outHost, elapsed, isError);
    }

    @Override
    public void updateInLink(
            long requestTime,
            Vertex inVertex,
            Vertex selfVertex,
            String selfHost,
            int elapsed, boolean isError
    ) {
        inLinkDao.inLink(requestTime, inVertex, selfVertex, selfHost, elapsed, isError);
    }

    @Override
    public void updateResponseTime(
            long requestTime,
            Vertex selfVertex,
            String agentId,
            int elapsed, boolean isError
    ) {
        responseTimeDao.received(requestTime, selfVertex, agentId, elapsed, isError);
    }

    @Override
    public void updateAgentState(
            long requestTime,
            @NotBlank final String outApplicationName,
            final ServiceType outServiceType,
            @NotBlank final String outAgentId
    ) {
        responseTimeDao.updatePing(requestTime, outApplicationName, outServiceType, outAgentId, 0, false);
    }
}
