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
package com.navercorp.pinpoint.realtime.dto.mapper.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;

import static com.navercorp.pinpoint.realtime.dto.mapper.grpc.MapperUtils.mapList;

/**
 * @author youngjin.kim2
 */
class ATDSupplyMapper {

    static ATDSupply from(GeneratedMessageV3 s) {
        if (s instanceof PCmdActiveThreadDumpRes) {
            return from((PCmdActiveThreadDumpRes) s);
        }
        if (s instanceof PCmdActiveThreadLightDumpRes) {
            return from((PCmdActiveThreadLightDumpRes) s);
        }
        throw new RuntimeException("Failed to map result");
    }

    static ATDSupply from(PCmdActiveThreadDumpRes s) {
        final ATDSupply t = new ATDSupply();
        t.setType(s.getType());
        t.setVersion(s.getVersion());
        t.setSubType(s.getSubType());
        t.setThreadDumps(mapList(s.getThreadDumpList(), ActiveThreadDumpMapper::from));
        return t;
    }

    static ATDSupply from(PCmdActiveThreadLightDumpRes s) {
        final ATDSupply t = new ATDSupply();
        t.setType(s.getType());
        t.setVersion(s.getVersion());
        t.setSubType(s.getSubType());
        t.setThreadDumps(mapList(s.getThreadDumpList(), ActiveThreadDumpMapper::fromLight));
        return t;
    }

    static PCmdActiveThreadDumpRes buildDetailedDumpResult(ATDSupply s) {
        return PCmdActiveThreadDumpRes.newBuilder()
                .setType(s.getType())
                .setVersion(s.getVersion())
                .setSubType(s.getSubType())
                .addAllThreadDump(mapList(s.getThreadDumps(), ActiveThreadDumpMapper::into))
                .build();
    }

    static PCmdActiveThreadLightDumpRes buildLightDumpResult(ATDSupply s) {
        return PCmdActiveThreadLightDumpRes.newBuilder()
                .setType(s.getType())
                .setVersion(s.getVersion())
                .setSubType(s.getSubType())
                .addAllThreadDump(mapList(s.getThreadDumps(), ActiveThreadDumpMapper::intoLight))
                .build();
    }

}
