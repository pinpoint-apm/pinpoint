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
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;

/**
 * @author youngjin.kim2
 */
public class GrpcDtoMapper {

    public static GeneratedMessageV3 buildGeneratedMessage(ATDDemand s) {
        return ATDDemandMapper.into(s);
    }

    public static ATDSupply buildATDSupply(GeneratedMessageV3 s) {
        return ATDSupplyMapper.from(s);
    }

    public static PCmdActiveThreadDumpRes buildDetailedDumpResult(ATDSupply s) {
        return ATDSupplyMapper.buildDetailedDumpResult(s);
    }

    public static PCmdActiveThreadLightDumpRes buildLightDumpResult(ATDSupply s) {
        return ATDSupplyMapper.buildLightDumpResult(s);
    }

}
