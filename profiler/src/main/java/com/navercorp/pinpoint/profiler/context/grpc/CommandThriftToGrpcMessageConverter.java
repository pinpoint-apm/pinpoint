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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCount;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;

import com.google.protobuf.GeneratedMessageV3;

/**
 * @author Taejin Koo
 */
public class CommandThriftToGrpcMessageConverter implements MessageConverter<GeneratedMessageV3> {

    @Override
    public GeneratedMessageV3 toMessage(Object message) {
        if (message instanceof TCommandEcho) {
            return buildPCommandEcho((TCommandEcho) message);
        } else if (message instanceof TCmdActiveThreadCount) {
            return buildPCmdActiveThreadCount((TCmdActiveThreadCount) message);
        } else if (message instanceof TCmdActiveThreadDump) {
            return buildPCmdActiveThreadDump((TCmdActiveThreadDump) message);
        } else if (message instanceof TCmdActiveThreadLightDump) {
            return buildPCmdActiveThreadLightDump((TCmdActiveThreadLightDump) message);
        }
        return null;
    }

    private PCmdEcho buildPCommandEcho(TCommandEcho tCommandEcho) {
        PCmdEcho.Builder builder = PCmdEcho.newBuilder();
        builder.setMessage(tCommandEcho.getMessage());
        return builder.build();
    }

    private PCmdActiveThreadCount buildPCmdActiveThreadCount(TCmdActiveThreadCount tCmdActiveThreadCount) {
        PCmdActiveThreadCount.Builder builder = PCmdActiveThreadCount.newBuilder();
        return builder.build();
    }

    private PCmdActiveThreadDump buildPCmdActiveThreadDump(TCmdActiveThreadDump tCmdActiveThreadDump) {
        PCmdActiveThreadDump.Builder builder = PCmdActiveThreadDump.newBuilder();
        builder.setLimit(tCmdActiveThreadDump.getLimit());
        if (tCmdActiveThreadDump.isSetLocalTraceIdList()) {
            builder.addAllLocalTraceId(tCmdActiveThreadDump.getLocalTraceIdList());
        }
        if (tCmdActiveThreadDump.isSetThreadNameList()) {
            builder.addAllThreadName(tCmdActiveThreadDump.getThreadNameList());
        }

        return builder.build();
    }

    private PCmdActiveThreadLightDump buildPCmdActiveThreadLightDump(TCmdActiveThreadLightDump tCmdActiveThreadLightDump) {
        PCmdActiveThreadLightDump.Builder builder = PCmdActiveThreadLightDump.newBuilder();
        builder.setLimit(tCmdActiveThreadLightDump.getLimit());
        if (tCmdActiveThreadLightDump.isSetLocalTraceIdList()) {
            builder.addAllLocalTraceId(tCmdActiveThreadLightDump.getLocalTraceIdList());
        }
        if (tCmdActiveThreadLightDump.isSetThreadNameList()) {
            builder.addAllThreadName(tCmdActiveThreadLightDump.getThreadNameList());
        }

        return builder.build();
    }

}
