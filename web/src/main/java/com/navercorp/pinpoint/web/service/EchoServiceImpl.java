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
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class EchoServiceImpl implements EchoService {

    private final AgentService agentService;

    public EchoServiceImpl(AgentService agentService) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
    }

    @Override
    public String echo(ClusterKey clusterKey, String message) {
        TCommandEcho echo = new TCommandEcho();
        echo.setMessage(message);

        try {
            PinpointRouteResponse pinpointRouteResponse = this.agentService.invoke(clusterKey, echo);
            if (pinpointRouteResponse != null && pinpointRouteResponse.getRouteResult() == TRouteResult.OK) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result == null) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "result null.");
                } else if (result instanceof TCommandEcho) {
                    return ((TCommandEcho) result).getMessage();
                } else if (result instanceof TResult) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, (((TResult) result).getMessage()));
                } else {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, result.toString());
                }
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown");
            }
        } catch (TException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
