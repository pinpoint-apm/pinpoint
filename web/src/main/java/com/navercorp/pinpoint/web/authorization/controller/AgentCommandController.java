/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadDumpFactory;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadDumpList;
import com.navercorp.pinpoint.web.vo.activethread.ThreadDumpResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@RestController
@RequestMapping("/agent")
public class AgentCommandController {
    private final ConfigProperties webProperties;
    private final AgentService agentService;
    private final ActiveThreadDumpService activeThreadDumpService;

    public AgentCommandController(
            ConfigProperties webProperties,
            AgentService agentService,
            ActiveThreadDumpService activeThreadDumpService
    ) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
        this.webProperties = Objects.requireNonNull(webProperties, "webProperties");
        this.activeThreadDumpService = Objects.requireNonNull(activeThreadDumpService, "activeThreadDumpService");
    }

    @GetMapping(value = "/activeThreadDump")
    public CodeResult<ThreadDumpResult> getActiveThreadDump(
            @RequestParam(value = "applicationName") String applicationName,
            @RequestParam(value = "agentId") String agentId,
            @RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
            @RequestParam(value = "threadName", required = false) List<String> threadNameList,
            @RequestParam(value = "localTraceId", required = false) List<Long> localTraceIdList
    ) {
        final ClusterKey clusterKey = getClusterKey(applicationName, agentId);

        final PCmdActiveThreadDumpRes response = this.activeThreadDumpService.getDetailedDump(
                clusterKey, threadNameList, localTraceIdList, limit);

        final AgentActiveThreadDumpList activeThreadDumpList = (new AgentActiveThreadDumpFactory())
                .create1(response.getThreadDumpList());

        return CodeResult.ok(new ThreadDumpResult(
                activeThreadDumpList,
                response.getType(),
                response.getSubType(),
                response.getVersion()
        ));
    }

    @GetMapping(value = "/activeThreadLightDump")
    public CodeResult<ThreadDumpResult> getActiveThreadLightDump(
            @RequestParam(value = "applicationName") String applicationName,
            @RequestParam(value = "agentId") String agentId,
            @RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
            @RequestParam(value = "threadName", required = false) List<String> threadNameList,
            @RequestParam(value = "localTraceId", required = false) List<Long> localTraceIdList
    ) {
        final ClusterKey clusterKey = getClusterKey(applicationName, agentId);
        final PCmdActiveThreadLightDumpRes response = this.activeThreadDumpService.getLightDump(
                clusterKey, threadNameList, localTraceIdList, limit);

        final AgentActiveThreadDumpList activeThreadDumpList = (new AgentActiveThreadDumpFactory())
                .create2(response.getThreadDumpList());

        return CodeResult.ok(new ThreadDumpResult(
                activeThreadDumpList,
                response.getType(),
                response.getSubType(),
                response.getVersion()
        ));
    }

    private ClusterKey getClusterKey(String applicationName, String agentId) {
        if (!webProperties.isEnableActiveThreadDump()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Disable activeThreadDump option. 'config.enable.activeThreadDump=false'");
        }

        final ClusterKey clusterKey = agentService.getClusterKey(applicationName, agentId);
        if (clusterKey == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Can't find suitable Agent(%s/%s)", applicationName, agentId));
        }
        return clusterKey;
    }

}
