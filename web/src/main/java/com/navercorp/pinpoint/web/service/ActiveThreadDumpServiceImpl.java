/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.mapper.ThriftToGrpcConverter;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadDumpServiceImpl implements ActiveThreadDumpService {

    private final AgentService agentService;

    public ActiveThreadDumpServiceImpl(AgentService agentService) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
    }

    @Override
    public PCmdActiveThreadDumpRes getDetailedDump(
            ClusterKey clusterKey,
            List<String> threadNames,
            List<Long> localTraceIds,
            int limit
    ) {
        TCmdActiveThreadDump threadDump = new TCmdActiveThreadDump();
        if (limit > 0) {
            threadDump.setLimit(limit);
        }
        if (threadNames != null) {
            threadDump.setThreadNameList(threadNames);
        }
        if (localTraceIds != null) {
            threadDump.setLocalTraceIdList(localTraceIds);
        }

        try {
            PinpointRouteResponse pinpointRouteResponse = agentService.invoke(clusterKey, threadDump);
            if (isSuccessResponse(pinpointRouteResponse)) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result instanceof TCmdActiveThreadDumpRes) {
                    return ThriftToGrpcConverter.convert((TCmdActiveThreadDumpRes) result);
                }
            }
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    handleFailedResponseMessage(pinpointRouteResponse)
            );
        } catch (TException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public PCmdActiveThreadLightDumpRes getLightDump(
            ClusterKey clusterKey,
            List<String> threadNames,
            List<Long> localTraceIds,
            int limit
    ) {
        TCmdActiveThreadLightDump threadDump = new TCmdActiveThreadLightDump();
        if (limit > 0) {
            threadDump.setLimit(limit);
        }
        if (threadNames != null) {
            threadDump.setThreadNameList(threadNames);
        }
        if (localTraceIds != null) {
            threadDump.setLocalTraceIdList(localTraceIds);
        }

        try {
            PinpointRouteResponse pinpointRouteResponse = agentService.invoke(clusterKey, threadDump);
            if (isSuccessResponse(pinpointRouteResponse)) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result instanceof TCmdActiveThreadLightDumpRes) {
                    return ThriftToGrpcConverter.convert((TCmdActiveThreadLightDumpRes) result);
                }
            }
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    handleFailedResponseMessage(pinpointRouteResponse)
            );
        } catch (TException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isSuccessResponse(PinpointRouteResponse pinpointRouteResponse) {
        if (pinpointRouteResponse == null) {
            return false;
        }

        TRouteResult routeResult = pinpointRouteResponse.getRouteResult();
        return routeResult == TRouteResult.OK;
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
