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

import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadDumpFactory;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadDumpList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.response.CodeResult;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AgentCommandController(ConfigProperties webProperties, AgentService agentService) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
        this.webProperties = Objects.requireNonNull(webProperties, "webProperties");
    }

    @GetMapping(value = "/activeThreadDump")
    public ResponseEntity<CodeResult> getActiveThreadDump(@RequestParam(value = "applicationName") String applicationName,
                                          @RequestParam(value = "agentId") String agentId,
                                          @RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
                                          @RequestParam(value = "threadName", required = false) List<String> threadNameList,
                                          @RequestParam(value = "localTraceId", required = false) List<Long> localTraceIdList) {
        if (!webProperties.isEnableActiveThreadDump()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Disable activeThreadDump option. 'config.enable.activeThreadDump=false'");

        }

        AgentInfo agentInfo = agentService.getAgentInfo(applicationName, agentId);
        if (agentInfo == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Can't find suitable Agent(%s/%s)", applicationName, agentId));
        }

        TCmdActiveThreadDump threadDump = new TCmdActiveThreadDump();
        if (limit > 0) {
            threadDump.setLimit(limit);
        }

        if (threadNameList != null) {
            threadDump.setThreadNameList(threadNameList);
        }
        if (localTraceIdList != null) {
            threadDump.setLocalTraceIdList(localTraceIdList);
        }

        try {
            PinpointRouteResponse pinpointRouteResponse = agentService.invoke(agentInfo, threadDump);
            if (isSuccessResponse(pinpointRouteResponse)) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result instanceof TCmdActiveThreadDumpRes) {
                    TCmdActiveThreadDumpRes activeThreadDumpResponse = (TCmdActiveThreadDumpRes) result;
                    List<TActiveThreadDump> activeThreadDumps = activeThreadDumpResponse.getThreadDumps();

                    AgentActiveThreadDumpFactory factory = new AgentActiveThreadDumpFactory();
                    AgentActiveThreadDumpList activeThreadDumpList = factory.create1(activeThreadDumps);

                    ThreadDumpResult responseData = createResponseData(activeThreadDumpList, activeThreadDumpResponse.getType(), activeThreadDumpResponse.getSubType(), activeThreadDumpResponse.getVersion());
                    return CodeResult.ok(responseData);
                }
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, handleFailedResponseMessage(pinpointRouteResponse));
        } catch (TException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isSuccessResponse(PinpointRouteResponse pinpointRouteResponse) {
        if (pinpointRouteResponse == null) {
            return false;
        }

        TRouteResult routeResult = pinpointRouteResponse.getRouteResult();
        if (routeResult != TRouteResult.OK) {
            return false;
        }

        return true;
    }

    private ThreadDumpResult createResponseData(AgentActiveThreadDumpList activeThreadDumpList, String type, String subType, String version) {
        return new ThreadDumpResult(activeThreadDumpList, type, subType, version);
    }

    public static class ThreadDumpResult {
        private final AgentActiveThreadDumpList threadDumpData;
        private final String type;
        private final String subType;
        private final String version;

        public ThreadDumpResult(AgentActiveThreadDumpList threadDumpData, String type, String subType, String version) {
            this.threadDumpData = threadDumpData;
            this.type = type;
            this.subType = subType;
            this.version = version;
        }

        public AgentActiveThreadDumpList getThreadDumpData() {
            return threadDumpData;
        }

        public String getType() {
            return type;
        }

        public String getSubType() {
            return subType;
        }

        public String getVersion() {
            return version;
        }
    }

    @GetMapping(value = "/activeThreadLightDump")
    public ResponseEntity<CodeResult> getActiveThreadLightDump(@RequestParam(value = "applicationName") String applicationName,
                                                               @RequestParam(value = "agentId") String agentId,
                                                               @RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
                                                               @RequestParam(value = "threadName", required = false) List<String> threadNameList,
                                                               @RequestParam(value = "localTraceId", required = false) List<Long> localTraceIdList) {
        if (!webProperties.isEnableActiveThreadDump()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Disable activeThreadDump option. 'config.enable.activeThreadDump=false'");
        }

        AgentInfo agentInfo = agentService.getAgentInfo(applicationName, agentId);
        if (agentInfo == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Can't find suitable Agent(%s/%s)", applicationName, agentId));
        }

        TCmdActiveThreadLightDump threadDump = new TCmdActiveThreadLightDump();
        if (limit > 0) {
            threadDump.setLimit(limit);
        }
        if (threadNameList != null) {
            threadDump.setThreadNameList(threadNameList);
        }
        if (localTraceIdList != null) {
            threadDump.setLocalTraceIdList(localTraceIdList);
        }

        try {
            PinpointRouteResponse pinpointRouteResponse = agentService.invoke(agentInfo, threadDump);
            if (isSuccessResponse(pinpointRouteResponse)) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result instanceof TCmdActiveThreadLightDumpRes) {
                    TCmdActiveThreadLightDumpRes activeThreadDumpResponse = (TCmdActiveThreadLightDumpRes) result;
                    List<TActiveThreadLightDump> activeThreadDumps = activeThreadDumpResponse.getThreadDumps();

                    AgentActiveThreadDumpFactory factory = new AgentActiveThreadDumpFactory();
                    AgentActiveThreadDumpList activeThreadDumpList = factory.create2(activeThreadDumps);

                    ThreadDumpResult responseData = createResponseData(activeThreadDumpList, activeThreadDumpResponse.getType(), activeThreadDumpResponse.getSubType(), activeThreadDumpResponse.getVersion());
                    return CodeResult.ok(responseData);
                }
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, handleFailedResponseMessage(pinpointRouteResponse));
        } catch (TException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String handleFailedResponseMessage(PinpointRouteResponse response) {
        if (response == null) {
            return "response is null";
        }

        TRouteResult routeResult = response.getRouteResult();
        if (routeResult != TRouteResult.OK) {
            return routeResult.name();
        } else {
            TBase<?, ?> tBase = response.getResponse();
            if (tBase instanceof TResult) {
                return ((TResult) tBase).getMessage();
            } else {
                return "unknown";
            }
        }
    }

}
