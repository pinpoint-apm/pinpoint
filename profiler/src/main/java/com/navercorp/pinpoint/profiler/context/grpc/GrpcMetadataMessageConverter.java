/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;

/**
 * @author jaehong.kim
 */
public class GrpcMetadataMessageConverter implements MessageConverter<GeneratedMessageV3> {


    public GrpcMetadataMessageConverter() {

    }

    @Override
    public GeneratedMessageV3 toMessage(Object message) {
        if (message instanceof SqlMetaData) {
            final SqlMetaData sqlMetaData = (SqlMetaData) message;
            return convertSqlMetaData(sqlMetaData);
        } else if (message instanceof ApiMetaData) {
            final ApiMetaData apiMetaData = (ApiMetaData) message;
            return convertApiMetaData(apiMetaData);
        } else if (message instanceof StringMetaData) {
            final StringMetaData stringMetaData = (StringMetaData) message;
            return convertStringMetaData(stringMetaData);
        }
        return null;
    }

    private PSqlMetaData convertSqlMetaData(final SqlMetaData sqlMetaData) {
        final PSqlMetaData.Builder builder = PSqlMetaData.newBuilder();
        builder.setSqlId(sqlMetaData.getSqlId());
        builder.setSql(sqlMetaData.getSql());
        return builder.build();
    }

    private PApiMetaData convertApiMetaData(final ApiMetaData apiMetaData) {
        final PApiMetaData.Builder builder = PApiMetaData.newBuilder();
        builder.setApiId(apiMetaData.getApiId());
        builder.setApiInfo(apiMetaData.getApiInfo());
        builder.setLine(apiMetaData.getLine());
        builder.setType(apiMetaData.getType());
        return builder.build();
    }

    private PStringMetaData convertStringMetaData(final StringMetaData stringMetaData) {
        final PStringMetaData.Builder builder = PStringMetaData.newBuilder();
        builder.setStringId(stringMetaData.getStringId());
        builder.setStringValue(stringMetaData.getStringValue());
        return builder.build();
    }
}