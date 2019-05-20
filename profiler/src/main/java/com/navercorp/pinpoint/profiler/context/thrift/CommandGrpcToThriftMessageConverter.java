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

import com.navercorp.pinpoint.grpc.trace.PActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
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
        } else if (message instanceof PCmdActiveThreadDumpRes) {
            return buildTCmdActiveThreadDumpRes((PCmdActiveThreadDumpRes) message);
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

    private TCmdActiveThreadDumpRes buildTCmdActiveThreadDumpRes(PCmdActiveThreadDumpRes pCmdActiveThreadDumpRes) {
        TCmdActiveThreadDumpRes tCmdActiveThreadDumpRes = new TCmdActiveThreadDumpRes();
        tCmdActiveThreadDumpRes.setVersion(pCmdActiveThreadDumpRes.getVersion());
        tCmdActiveThreadDumpRes.setType(pCmdActiveThreadDumpRes.getType());
        tCmdActiveThreadDumpRes.setSubType(pCmdActiveThreadDumpRes.getSubType());

        for (PActiveThreadDump pActiveThreadDump : pCmdActiveThreadDumpRes.getThreadDumpList()) {
            tCmdActiveThreadDumpRes.addToThreadDumps(buildTActiveThreadDump(pActiveThreadDump));
        }

        return tCmdActiveThreadDumpRes;
    }

    private TActiveThreadDump buildTActiveThreadDump(PActiveThreadDump pActiveThreadDump) {
        TActiveThreadDump tActiveThreadDump = new TActiveThreadDump();
        tActiveThreadDump.setStartTime(pActiveThreadDump.getStartTime());
        tActiveThreadDump.setSampled(pActiveThreadDump.getSampled());

        if (pActiveThreadDump.getSampled()) {
            tActiveThreadDump.setLocalTraceId(pActiveThreadDump.getLocalTraceId());
            tActiveThreadDump.setTransactionId(pActiveThreadDump.getTransactionId());
            tActiveThreadDump.setEntryPoint(pActiveThreadDump.getEntryPoint());
        }

        tActiveThreadDump.setThreadDump(buildTThreadDump(pActiveThreadDump.getThreadDump()));
        return tActiveThreadDump;
    }

    private TThreadDump buildTThreadDump(PThreadDump pThreadDump) {
        TThreadDump tThreadDump = new TThreadDump();
        tThreadDump.setThreadName(pThreadDump.getThreadName());
        tThreadDump.setThreadId(pThreadDump.getThreadId());
        tThreadDump.setBlockedTime(pThreadDump.getBlockedTime());
        tThreadDump.setBlockedCount(pThreadDump.getBlockedCount());
        tThreadDump.setWaitedTime(pThreadDump.getWaitedTime());
        tThreadDump.setWaitedCount(pThreadDump.getWaitedCount());

        tThreadDump.setInNative(pThreadDump.getInNative());
        tThreadDump.setSuspended(pThreadDump.getSuspended());
        tThreadDump.setThreadState(TThreadState.findByValue(pThreadDump.getThreadStateValue()));

        for (String stackTrace : pThreadDump.getStackTraceList()) {
            tThreadDump.addToStackTrace(stackTrace);
        }

        for (PMonitorInfo pMonitorInfo : pThreadDump.getLockedMonitorList()) {
            final TMonitorInfo tMonitorInfo = new TMonitorInfo();
            tMonitorInfo.setStackDepth(pMonitorInfo.getStackDepth());
            tMonitorInfo.setStackFrame(pMonitorInfo.getStackFrame());
            tThreadDump.addToLockedMonitors(tMonitorInfo);
        }


        tThreadDump.setLockName(pThreadDump.getLockName());
        tThreadDump.setLockOwnerId(pThreadDump.getLockOwnerId());
        tThreadDump.setLockOwnerName(pThreadDump.getLockOwnerName());
        for (String lockedSynchronizer : pThreadDump.getLockedSynchronizerList()) {
            tThreadDump.addToLockedSynchronizers(lockedSynchronizer);
        }

        return tThreadDump;
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
