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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadDump;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadDumpList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
@Controller
@RequestMapping("/agent")
public class AgentCommandController {

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    @Autowired
    private AgentService agentService;

    @RequestMapping(value = "/activeThreadDump", method = RequestMethod.GET)
    public ModelAndView getActiveThreadDump(@RequestParam(value = "applicationName") String applicationName,
                                            @RequestParam(value = "agentId") String agentId,
                                            @RequestParam(value = "startTimeStamp") long startTimeStamp,
                                            @RequestParam(value = "threadName", required = false) String[] threadNameList,
                                            @RequestParam(value = "traceId", required = false) Long[] traceIdList) throws TException {
        AgentInfo agentInfo = agentService.getAgentInfo(applicationName, agentId, startTimeStamp);
        if (agentInfo == null) {
            return createResponse(false, String.format("Can't find suitable Agent(%s/%s/%d)", applicationName, agentId, startTimeStamp));
        }

        TCmdActiveThreadDump threadDump = new TCmdActiveThreadDump();
        if (threadNameList != null) {
            threadDump.setTargetThreadNameList(Arrays.asList(threadNameList));
        }
        if (traceIdList != null) {
            threadDump.setTraceIdList(Arrays.asList(traceIdList));
        }

        try {
            PinpointRouteResponse pinpointRouteResponse = agentService.invoke(agentInfo, threadDump);
            if (pinpointRouteResponse != null && pinpointRouteResponse.getRouteResult() == TRouteResult.OK) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result instanceof TCmdActiveThreadDumpRes) {
                    TCmdActiveThreadDumpRes activeThreadDumpResponse = (TCmdActiveThreadDumpRes) result;

                    AgentActiveThreadDumpList activeThreadDumpList = new AgentActiveThreadDumpList(activeThreadDumpResponse.getThreadDumpsSize());
                    List<TActiveThreadDump> activeThreadDumps = activeThreadDumpResponse.getThreadDumps();
                    if (activeThreadDumps != null) {
                        for (TActiveThreadDump activeThreadDump : activeThreadDumps) {
                            activeThreadDumpList.add(new AgentActiveThreadDump(activeThreadDump));
                        }
                    }

                    Map<String, Object> response =new HashMap<>(3);
                    response.put("threadDumpData", activeThreadDumpList);
                    response.put("jvmType", activeThreadDumpResponse.getJvmType());
                    response.put("jvmVersion", activeThreadDumpResponse.getJvmVersion());

                    return createResponse(true, response);
                } else {
                    return handleFailedResponse(result);
                }
            } else {
                return createResponse(false, "unknown");
            }
        } catch (TException e) {
            return createResponse(false, e.getMessage());
        }
    }

    @RequestMapping(value = "/activeThreadLightDump", method = RequestMethod.GET)
    public ModelAndView getActiveThreadLightDump(@RequestParam(value = "applicationName") String applicationName,
                                            @RequestParam(value = "agentId") String agentId,
                                            @RequestParam(value = "startTimeStamp") long startTimeStamp,
                                            @RequestParam(value = "threadName", required = false) String[] threadNameList,
                                            @RequestParam(value = "traceId", required = false) Long[] traceIdList) throws TException {
        AgentInfo agentInfo = agentService.getAgentInfo(applicationName, agentId, startTimeStamp);
        if (agentInfo == null) {
            return createResponse(false, String.format("Can't find suitable Agent(%s/%s/%d)", applicationName, agentId, startTimeStamp));
        }

        TCmdActiveThreadLightDump threadDump = new TCmdActiveThreadLightDump();
        if (threadNameList != null) {
            threadDump.setTargetThreadNameList(Arrays.asList(threadNameList));
        }
        if (traceIdList != null) {
            threadDump.setTraceIdList(Arrays.asList(traceIdList));
        }

        try {
            PinpointRouteResponse pinpointRouteResponse = agentService.invoke(agentInfo, threadDump);
            if (pinpointRouteResponse != null && pinpointRouteResponse.getRouteResult() == TRouteResult.OK) {
                TBase<?, ?> result = pinpointRouteResponse.getResponse();
                if (result instanceof TCmdActiveThreadLightDumpRes) {
                    TCmdActiveThreadLightDumpRes activeThreadDumpResponse = (TCmdActiveThreadLightDumpRes) result;

                    AgentActiveThreadDumpList activeThreadDumpList = new AgentActiveThreadDumpList(activeThreadDumpResponse.getThreadDumpsSize());
                    List<TActiveThreadLightDump> activeThreadDumps = activeThreadDumpResponse.getThreadDumps();
                    if (activeThreadDumps != null) {
                        for (TActiveThreadLightDump activeThreadDump : activeThreadDumps) {
                            activeThreadDumpList.add(new AgentActiveThreadDump(activeThreadDump));
                        }
                    }

                    Map<String, Object> response =new HashMap<>(3);
                    response.put("threadDumpData", activeThreadDumpList);
                    response.put("jvmType", activeThreadDumpResponse.getJvmType());
                    response.put("jvmVersion", activeThreadDumpResponse.getJvmVersion());

                    return createResponse(true, response);
                } else {
                    return handleFailedResponse(result);
                }
            } else {
                return createResponse(false, "unknown");
            }
        } catch (TException e) {
            return createResponse(false, e.getMessage());
        }
    }

    private ModelAndView handleFailedResponse(TBase<?, ?> failedResponse) {
        if (failedResponse == null) {
            return createResponse(false, "result null");
        } else if (failedResponse instanceof TResult) {
            return createResponse(false, ((TResult) failedResponse).getMessage());
        } else {
            return createResponse(false, failedResponse.toString());
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
