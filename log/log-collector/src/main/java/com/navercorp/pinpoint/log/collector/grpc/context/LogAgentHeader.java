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

import com.navercorp.pinpoint.log.vo.FileKey;
import io.grpc.Context;
import io.grpc.Metadata;

/**
 * @author youngjin.kim2
 */
public class LogAgentHeader {

    public static Metadata.Key<String> HOST_GROUP_NAME_KEY = keyOf("hostgroupname");
    public static Metadata.Key<String> HOST_NAME_KEY = keyOf("hostname");
    public static Metadata.Key<String> FILE_NAME_KEY = keyOf("filename");

    public static final Context.Key<LogAgentHeader> LOG_AGENT_HEADER_KEY = Context.key("logagentheader");

    private final FileKey fileKey;

    public LogAgentHeader(FileKey fileKey) {
        this.fileKey = fileKey;
    }

    public FileKey getFileKey() {
        return fileKey;
    }

    private static Metadata.Key<String> keyOf(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }

}
