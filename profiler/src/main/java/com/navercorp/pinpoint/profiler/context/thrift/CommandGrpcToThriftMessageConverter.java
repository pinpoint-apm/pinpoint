/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.grpc.trace.PActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;

import org.apache.thrift.TBase;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class CommandGrpcToThriftMessageConverter implements MessageConverter<TBase> {

    @Override
    public TBase toMessage(Object message) {
        if (message instanceof PCmdEchoResponse) {
            return buildTCommandEcho((PCmdEchoResponse) message);
        } else if (message instanceof PCmdActiveThreadCountRes) {
            return buildTCmdActiveThreadCountRes((PCmdActiveThreadCountRes) message);
        } else if (message instanceof PCmdActiveThreadLightDumpRes) {
            return buildTCmdActiveThreadLightDumpRes((PCmdActiveThreadLightDumpRes) message);
        }
        return null;
    }

    private TCommandEcho buildTCommandEcho(PCmdEchoResponse echoMessage) {
        String message = echoMessage.getMessage();
        return new TCommandEcho(message);
    }

    private TCmdActiveThreadCountRes buildTCmdActiveThreadCountRes(PCmdActiveThreadCountRes activeThreadCountRes) {
        int histogramSchemaType = activeThreadCountRes.getHistogramSchemaType();
        List<Integer> activeThreadCountList = activeThreadCountRes.getActiveThreadCountList();
        long timeStamp = activeThreadCountRes.getTimeStamp();

        TCmdActiveThreadCountRes result = new TCmdActiveThreadCountRes(histogramSchemaType, activeThreadCountList);
        result.setTimeStamp(timeStamp);

        return result;
    }

    private TCmdActiveThreadLightDumpRes buildTCmdActiveThreadLightDumpRes(PCmdActiveThreadLightDumpRes pCmdActiveThreadLightDumpRes) {
        TCmdActiveThreadLightDumpRes tCmdActiveThreadLightDumpRes = new TCmdActiveThreadLightDumpRes();
        tCmdActiveThreadLightDumpRes.setVersion(pCmdActiveThreadLightDumpRes.getVersion());
        tCmdActiveThreadLightDumpRes.setType(pCmdActiveThreadLightDumpRes.getType());
        tCmdActiveThreadLightDumpRes.setSubType(pCmdActiveThreadLightDumpRes.getSubType());

        for (PActiveThreadLightDump pActiveThreadLightDump : pCmdActiveThreadLightDumpRes.getThreadDumpList()) {
            tCmdActiveThreadLightDumpRes.addToThreadDumps(buildTActiveThreadLightDump(pActiveThreadLightDump));
        }

        return tCmdActiveThreadLightDumpRes;
    }

    private TActiveThreadLightDump buildTActiveThreadLightDump(PActiveThreadLightDump pActiveThreadLightDump) {
        TActiveThreadLightDump tActiveThreadLightDump = new TActiveThreadLightDump();
        tActiveThreadLightDump.setStartTime(pActiveThreadLightDump.getStartTime());
        tActiveThreadLightDump.setSampled(pActiveThreadLightDump.getSampled());

        if (pActiveThreadLightDump.getSampled()) {
            tActiveThreadLightDump.setLocalTraceId(pActiveThreadLightDump.getLocalTraceId());
            tActiveThreadLightDump.setTransactionId(pActiveThreadLightDump.getTransactionId());
            tActiveThreadLightDump.setEntryPoint(pActiveThreadLightDump.getEntryPoint());
        }

        tActiveThreadLightDump.setThreadDump(buildTThreadLightDump(pActiveThreadLightDump.getThreadDump()));
        return tActiveThreadLightDump;
    }

    private TThreadLightDump buildTThreadLightDump(PThreadLightDump pThreadDump) {
        TThreadLightDump tThreadLightDump = new TThreadLightDump();
        tThreadLightDump.setThreadId(pThreadDump.getThreadId());
        tThreadLightDump.setThreadName(pThreadDump.getThreadName());
        tThreadLightDump.setThreadState(TThreadState.findByValue(pThreadDump.getThreadStateValue()));
        return tThreadLightDump;
    }

}
