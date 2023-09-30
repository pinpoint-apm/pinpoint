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
package com.navercorp.pinpoint.realtime.collector.mapper;

import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;

import static com.navercorp.pinpoint.realtime.collector.mapper.MapperUtils.mapList;

/**
 * @author youngjin.kim2
 */
public class ATDSupplyMapper {

    public static ATDSupply from(PCmdActiveThreadDumpRes s) {
        ATDSupply t = new ATDSupply();
        t.setType(s.getType());
        t.setVersion(s.getVersion());
        t.setSubType(s.getSubType());
        t.setThreadDumps(mapList(s.getThreadDumpList(), ActiveThreadDumpMapper::from));
        return t;
    }

    public static ATDSupply from(PCmdActiveThreadLightDumpRes s) {
        ATDSupply t = new ATDSupply();
        t.setType(s.getType());
        t.setVersion(s.getVersion());
        t.setSubType(s.getSubType());
        t.setThreadDumps(mapList(s.getThreadDumpList(), ActiveThreadDumpMapper::fromLight));
        return t;
    }

}
