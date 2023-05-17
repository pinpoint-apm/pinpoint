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
package com.navercorp.pinpoint.realtime.dto.mapper;

import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import org.apache.thrift.TBase;

/**
 * @author youngjin.kim2
 */
@SuppressWarnings("ALL")
public class ATDSupplyMapper {

    public static ATDSupply fromThrift(TBase<?, ?> s) {
        if (s instanceof TCmdActiveThreadDumpRes) {
            return fromThrift((TCmdActiveThreadDumpRes) s);
        }
        if (s instanceof TCmdActiveThreadLightDumpRes) {
            return fromThrift((TCmdActiveThreadLightDumpRes) s);
        }
        return null;
    }

    public static ATDSupply fromThrift(TCmdActiveThreadDumpRes s) {
        final ATDSupply t = new ATDSupply();
        t.setType(s.getType());
        t.setSubType(s.getSubType());
        t.setVersion(s.getVersion());
        t.setThreadDumps(MapperUtils.mapList(s.getThreadDumps(), ActiveThreadDumpMapper::fromThrift));
        return t;
    }

    public static ATDSupply fromThrift(TCmdActiveThreadLightDumpRes s) {
        final ATDSupply t = new ATDSupply();
        t.setType(s.getType());
        t.setSubType(s.getSubType());
        t.setVersion(s.getVersion());
        t.setThreadDumps(MapperUtils.mapList(s.getThreadDumps(), ActiveThreadDumpMapper::fromThrift));
        return t;
    }

    public static TCmdActiveThreadDumpRes toThriftDetailed(ATDSupply s) {
        if (s == null) {
            return null;
        }
        final TCmdActiveThreadDumpRes t = new TCmdActiveThreadDumpRes();
        t.setType(s.getType());
        t.setSubType(s.getSubType());
        t.setVersion(s.getVersion());
        t.setThreadDumps(MapperUtils.mapList(s.getThreadDumps(), ActiveThreadDumpMapper::toThriftDetailed));
        return t;
    }

    public static TCmdActiveThreadLightDumpRes toThriftLight(ATDSupply s) {
        if (s == null) {
            return null;
        }
        final TCmdActiveThreadLightDumpRes t = new TCmdActiveThreadLightDumpRes();
        t.setType(s.getType());
        t.setSubType(s.getSubType());
        t.setVersion(s.getVersion());
        t.setThreadDumps(MapperUtils.mapList(s.getThreadDumps(), ActiveThreadDumpMapper::toThriftLight));
        return t;
    }

}
