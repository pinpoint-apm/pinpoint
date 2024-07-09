/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.service;

import com.navercorp.pinpoint.collector.applicationmap.dao.InboundDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.OutboundDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.SelfDao;
import com.navercorp.pinpoint.common.trace.ServiceType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * @author intr3p1d
 */
@Service
@Validated
public class ApplicationMapService {

    private final InboundDao inboundDao;
    private final OutboundDao outboundDao;
    private final SelfDao selfDao;

    public ApplicationMapService(
            InboundDao inboundDao,
            OutboundDao outboundDao,
            SelfDao selfDao
    ) {
        this.inboundDao = Objects.requireNonNull(inboundDao, "inboundDao");
        this.outboundDao = Objects.requireNonNull(outboundDao, "outboundDao");
        this.selfDao = Objects.requireNonNull(selfDao, "selfDao");
    }

    public void updateBidirectional(
            @NotBlank String srcServiceGroup,
            @NotBlank String srcApplicationName, ServiceType srcServiceType,
            @NotBlank String srcHost,
            @NotBlank String destServiceGroup,
            @NotBlank String destApplicationName, ServiceType destServiceType,
            @NotBlank String destHost,
            int elapsed, boolean isError
    ) {
        // src -> dest
        // inbound (rowKey dest <- columnName src)
        // outbound (rowKey src -> columnName dest)

        updateOutbound(
                srcServiceGroup, srcApplicationName, srcServiceType,
                destServiceGroup, destApplicationName, destServiceType,
                srcHost, elapsed, isError
        );

        updateInbound(
                srcServiceGroup, srcApplicationName, srcServiceType,
                destServiceGroup, destApplicationName, destServiceType,
                srcHost, elapsed, isError
        );

    }


    public void updateInbound(
            @NotBlank String srcServiceGroup, @NotBlank String srcApplicationName, ServiceType srcServiceType,
            @NotBlank String destServiceGroup, @NotBlank String destApplicationName, ServiceType destServiceType,
            @NotBlank String srcHost, int elapsed, boolean isError
    ) {
        // inbound (rowKey dest <- columnName src)
        inboundDao.update(
                srcServiceGroup, srcApplicationName, srcServiceType,
                destServiceGroup, destApplicationName, destServiceType,
                srcHost, elapsed, isError
        );
    }

    public void updateOutbound(
            @NotBlank String srcServiceGroup, @NotBlank String srcApplicationName, ServiceType srcServiceType,
            @NotBlank String destServiceGroup, @NotBlank String destApplicationName, ServiceType destServiceType,
            @NotBlank String srcHost, int elapsed, boolean isError
    ) {
        // outbound (rowKey src -> columnName dest)
        outboundDao.update(
                srcServiceGroup, srcApplicationName, srcServiceType,
                destServiceGroup, destApplicationName, destServiceType,
                srcHost, elapsed, isError
        );
    }

    public void updateSelfResponseTime(
            @NotBlank String serviceGroup, @NotBlank String applicationName, ServiceType applicationServiceType,
            int elapsed, boolean isError
    ) {
        selfDao.received(
                serviceGroup, applicationName, applicationServiceType, elapsed, isError
        );
    }

    public void updateAgentState(
            @NotBlank String serviceGroup, @NotBlank String applicationName, ServiceType applicationServiceType
    ) {
        selfDao.updatePing(
                serviceGroup, applicationName, applicationServiceType, 0, false
        );
    }
}
