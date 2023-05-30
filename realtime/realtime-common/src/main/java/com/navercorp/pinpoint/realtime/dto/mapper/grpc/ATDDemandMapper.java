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
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDump;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;

import static com.navercorp.pinpoint.realtime.dto.mapper.grpc.MapperUtils.nonNullList;

/**
 * @author youngjin.kim2
 */
class ATDDemandMapper {

    static GeneratedMessageV3 into(ATDDemand s) {
        if (s.isLight()) {
            return intoLight(s);
        } else {
            return intoDetailed(s);
        }
    }

    static PCmdActiveThreadDump intoDetailed(ATDDemand s) {
        return PCmdActiveThreadDump.newBuilder()
                .addAllThreadName(nonNullList(s.getThreadNameList()))
                .addAllLocalTraceId(nonNullList(s.getLocalTraceIdList()))
                .setLimit(s.getLimit())
                .build();
    }

    static PCmdActiveThreadLightDump intoLight(ATDDemand s) {
        return PCmdActiveThreadLightDump.newBuilder()
                .addAllThreadName(nonNullList(s.getThreadNameList()))
                .addAllLocalTraceId(nonNullList(s.getLocalTraceIdList()))
                .setLimit(s.getLimit())
                .build();
    }

}
