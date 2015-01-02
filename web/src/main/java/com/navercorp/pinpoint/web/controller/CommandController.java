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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDumpResponse;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import com.navercorp.pinpoint.web.server.PinpointSocketManager;

@Controller
@RequestMapping("/command")
public class CommandController {

    // FIX ME: created for a simple ping/pong test for now
    // need a formal set of APIs and proper code

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    @Autowired
    private PinpointSocketManager socketManager;

    @RequestMapping(value = "/echo", method = RequestMethod.GET)
    public ModelAndView echo(@RequestParam("application") String applicationName, @RequestParam("agent") String agentId,
            @RequestParam("startTimeStamp") long startTimeStamp, @RequestParam("message") String message) throws TException {

        ChannelContext context = socketManager.getCollectorChannelContext(applicationName, agentId, startTimeStamp);

        if (context == null) {
            return createResponse(false, String.format("Can't find suitable ChannelContext(%s/%s/%d).", applicationName, agentId, startTimeStamp));
        }

        TCommandEcho echo = new TCommandEcho();
        echo.setMessage(message);

        byte[] payload = serialize(echo);

        TCommandTransfer transfer = new TCommandTransfer();
        transfer.setApplicationName(applicationName);
        transfer.setAgentId(agentId);
        transfer.setStartTime(startTimeStamp);
        transfer.setPayload(payload);

        Future<ResponseMessage> future = context.getSocketChannel().sendRequestMessage(serialize(transfer));
        future.await();

        String exceptionMessage = StringUtils.EMPTY;

        ResponseMessage responseMessage = future.getResult();
        try {
            TBase result = deserialize(responseMessage.getMessage());

            if (result == null) {
                return createResponse(false, String.format("Can't get message from %s.", context));
            } else if (result instanceof TCommandEcho) {
                return createResponse(true, ((TCommandEcho) result).getMessage());
            } else if (result instanceof TResult) {
                return createResponse(false, ((TResult) result).getMessage());
            } else {
                return createResponse(false, result.toString());
            }

        } catch (TException e) {
            exceptionMessage = e.getMessage();
        }

        return createResponse(false, exceptionMessage);
    }

    @RequestMapping(value = "/threadDump", method = RequestMethod.GET)
    public ModelAndView echo(@RequestParam("application") String applicationName, @RequestParam("agent") String agentId,
            @RequestParam("startTimeStamp") long startTimeStamp) throws TException {

        ChannelContext context = socketManager.getCollectorChannelContext(applicationName, agentId, startTimeStamp);

        if (context == null) {
            return createResponse(false, String.format("Can't find suitable ChannelContext(%s/%s/%d).", applicationName, agentId, startTimeStamp));
        }

        TCommandThreadDump threadDump = new TCommandThreadDump();

        byte[] payload = serialize(threadDump);

        TCommandTransfer transfer = new TCommandTransfer();
        transfer.setApplicationName(applicationName);
        transfer.setAgentId(agentId);
        transfer.setStartTime(startTimeStamp);
        transfer.setPayload(payload);

        Future<ResponseMessage> future = context.getSocketChannel().sendRequestMessage(serialize(transfer));
        future.await();

        String exceptionMessage = StringUtils.EMPTY;

        ResponseMessage responseMessage = future.getResult();
        try {
            TBase result = deserialize(responseMessage.getMessage());

            if (result == null) {
                return createResponse(false, String.format("Can't get message from %s.", context));
            } else if (result instanceof TCommandThreadDumpResponse) {
                Map<String, String> map = createThreadDump((TCommandThreadDumpResponse) result);

                logger.debug("{}", map.toString());

                return createResponse(true, map);
            } else if (result instanceof TResult) {
                return createResponse(false, ((TResult) result).getMessage());
            } else {
                return createResponse(false, result.toString());
            }

        } catch (TException e) {
            exceptionMessage = e.getMessage();
        }

        return createResponse(false, exceptionMessage);
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

    private Map<String, String> createThreadDump(TCommandThreadDumpResponse threadDumps) {

        Map<String, String> map = new HashMap<String, String>();

        for (TThreadDump threadDump : threadDumps.getThreadDumps()) {
            String dump = threadDumptoString(threadDump);

            map.put(threadDump.getThreadName(), dump);
        }

        return map;
    }

    public String threadDumptoString(TThreadDump threadDump) {
        StringBuilder sb = new StringBuilder("\"" + threadDump.getThreadName() + "\"" + " Id=" + threadDump.getThreadId() + " "
                + threadDump.getThreadState().name());
        if (!StringUtils.isBlank(threadDump.getLockName())) {
            sb.append(" on ").append(threadDump.getLockName());
        }

        if (!StringUtils.isBlank(threadDump.getLockOwnerName())) {
            sb.append(" owned by \"").append(threadDump.getLockOwnerName()).append("\" Id=").append(threadDump.getLockOwnerId());
        }

        if (threadDump.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadDump.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');

        for (int i = 0; i < threadDump.getStackTraceSize(); i++) {
            String ste = threadDump.getStackTrace().get(i);
            sb.append("\tat ").append(ste);
            sb.append('\n');

            if (i == 0 && !StringUtils.isBlank(threadDump.getLockName())) {
                TThreadState ts = threadDump.getThreadState();
                switch (ts) {
                case BLOCKED:
                    sb.append("\t-  blocked on ").append(threadDump.getLockName());
                    sb.append('\n');
                    break;
                case WAITING:
                    sb.append("\t-  waiting on ").append(threadDump.getLockName());
                    sb.append('\n');
                    break;
                case TIMED_WAITING:
                    sb.append("\t-  waiting on ").append(threadDump.getLockName());
                    sb.append('\n');
                    break;
                default:
                }
            }

            if (threadDump.getLockedMonitors() != null) {
                for (TMonitorInfo mi : threadDump.getLockedMonitors()) {
                    if (mi.getStackDepth() == i) {
                        sb.append("\t-  locked ").append(mi.getStackFrame());
                        sb.append('\n');
                    }
                }
            }
        }

        List<String> locks = threadDump.getLockedSynchronizers();
        if (locks != null) {
            if (locks.size() > 0) {
                sb.append("\n\tNumber of locked synchronizers = ").append(locks.size());
                sb.append('\n');
                for (String li : locks) {
                    sb.append("\t- ").append(li);
                    sb.append('\n');
                }
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    private byte[] serialize(TBase result) throws TException {
        return SerializationUtils.serialize(result, commandSerializerFactory);
    }

    private TBase deserialize(byte[] objectData) throws TException {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory);
    }

}
