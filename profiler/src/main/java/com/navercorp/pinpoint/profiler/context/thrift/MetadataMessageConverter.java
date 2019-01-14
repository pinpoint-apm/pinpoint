/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;
import org.apache.thrift.TBase;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MetadataMessageConverter implements MessageConverter<TBase<?, ?>> {

    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;

    public MetadataMessageConverter(String applicationName, String agentId, long agentStartTime) {
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName must not be null");
        this.agentId = Assert.requireNonNull(agentId, "agentId must not be null");
        this.agentStartTime = agentStartTime;
    }

    @Override
    public TBase<?, ?> toMessage(Object message) {
        if (message instanceof SqlMetaData) {
            final SqlMetaData sqlMetaData = (SqlMetaData) message;
            return new TSqlMetaData(agentId, agentStartTime, sqlMetaData.getSqlId(), sqlMetaData.getSql());
        }
        if (message instanceof ApiMetaData) {
            final ApiMetaData apiMetaData = (ApiMetaData) message;
            final TApiMetaData tApiMetaData = new TApiMetaData(agentId, agentStartTime, apiMetaData.getApiId(), apiMetaData.getApiInfo());
            tApiMetaData.setLine(apiMetaData.getLine());
            tApiMetaData.setType(apiMetaData.getType());
            return tApiMetaData;
        }
        if (message instanceof StringMetaData) {
            final StringMetaData stringMetaData = (StringMetaData) message;
            return new TStringMetaData(agentId, agentStartTime, stringMetaData.getStringId(), stringMetaData.getStringValue());
        }
        return null;
    }
}
