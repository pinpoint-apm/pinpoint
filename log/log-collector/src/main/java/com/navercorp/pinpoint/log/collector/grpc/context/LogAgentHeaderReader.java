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
package com.navercorp.pinpoint.log.collector.grpc.context;

import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.log.vo.FileKey;
import io.grpc.Metadata;
import io.grpc.Status;

/**
 * @author youngjin.kim2
 */
public class LogAgentHeaderReader implements HeaderReader<LogAgentHeader> {

    @Override
    public LogAgentHeader extract(Metadata headers) {
        final String hostGroupName = getString(headers, LogAgentHeader.HOST_GROUP_NAME_KEY);
        final String hostName = getString(headers, LogAgentHeader.HOST_NAME_KEY);
        final String fileName = getString(headers, LogAgentHeader.FILE_NAME_KEY);
        final FileKey fileKey = FileKey.of(hostGroupName, hostName, fileName);
        return new LogAgentHeader(fileKey);
    }

    private static String getString(Metadata headers, Metadata.Key<String> key) {
        final String value = headers.get(key);
        if (value == null) {
            throw Status.INVALID_ARGUMENT.withDescription(key.name() + " header is missing").asRuntimeException();
        }
        return value;
    }

}
