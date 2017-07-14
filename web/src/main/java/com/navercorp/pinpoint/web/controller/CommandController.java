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

import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/command")
public class CommandController {

    // FIX ME: created for a simple ping/pong test for now
    // need a formal set of APIs and proper code

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    @Qualifier("commandHeaderTBaseDeserializerFactory")
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    @Autowired
    private AgentService agentService;

    @RequestMapping(value = "/echo", method = RequestMethod.GET)
    public ModelAndView echo(@RequestParam("applicationName") String applicationName, @RequestParam("agentId") String agentId,
                             @RequestParam("startTimeStamp") long startTimeStamp, @RequestParam("message") String message) throws TException {

        AgentInfo agentInfo = agentService.getAgentInfo(applicationName, agentId, startTimeStamp);
        if (agentInfo == null) {
            return createResponse(false, String.format("Can't find suitable PinpointServer(%s/%s/%d).", applicationName, agentId, startTimeStamp));
        }

        TCommandEcho echo = new TCommandEcho();
        echo.setMessage(message);

        try {
            PinpointRouteResponse pinpointRouteResponse = agentService.invoke(agentInfo, echo);
            if (pinpointRouteResponse != null && pinpointRouteResponse.getRouteResult() == TRouteResult.OK) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result == null) {
                    return createResponse(false, "result null.");
                } else if (result instanceof TCommandEcho) {
                    return createResponse(true, ((TCommandEcho) result).getMessage());
                } else if (result instanceof TResult) {
                    return createResponse(false, ((TResult) result).getMessage());
                } else {
                    return createResponse(false, result.toString());
                }
            } else {
                return createResponse(false, "unknown");
            }
        } catch (TException e) {
            return createResponse(false, e.getMessage());
        }
    }

    private ModelAndView createResponse(boolean success, Object message) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("jsonView");

        if (success) {
            mv.addObject("code", 0);
        } else {
            mv.addObject("code", -1);
        }

        mv.addObject("message", message);

        return mv;
    }

}
