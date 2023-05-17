/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.service.EchoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@RequestMapping("/command")
public class CommandController {

    // FIX ME: created for a simple ping/pong test for now
    // need a formal set of APIs and proper code

    private final AgentService agentService;
    private final EchoService echoService;

    public CommandController(AgentService agentService, EchoService echoService) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
        this.echoService = Objects.requireNonNull(echoService, "echoService");
    }

    @GetMapping(value = "/echo")
    public CodeResult<String> echo(@RequestParam("applicationName") String applicationName, @RequestParam("agentId") String agentId,
                                          @RequestParam("startTimeStamp") long startTimeStamp, @RequestParam("message") String message) {

        final ClusterKey clusterKey = agentService.getClusterKey(applicationName, agentId, startTimeStamp);
        if (clusterKey == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Can't find suitable PinpointServer(%s/%s/%d).", applicationName, agentId, startTimeStamp));
        }

        return CodeResult.ok(this.echoService.echo(clusterKey, message));
    }

}
