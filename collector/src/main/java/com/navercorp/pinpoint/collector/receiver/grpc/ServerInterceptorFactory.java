/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.common.server.uid.ObjectNameVersion;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import com.navercorp.pinpoint.grpc.server.ServerHeaderReaderAdaptor;
import com.navercorp.pinpoint.grpc.server.ServerHeaderReaderV1;
import com.navercorp.pinpoint.grpc.server.ServerHeaderReaderV4;
import io.grpc.Metadata;
import io.grpc.ServerInterceptor;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ServerInterceptorFactory {

    private final String name;
    private final boolean v3;

    public static Map<String, Object> emptyProperties(Metadata headers) {
        return Collections.emptyMap();
    }

    public ServerInterceptorFactory(String name, ObjectNameVersion version) {
        this.name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
        this.v3 = resolveVersion(version);
    }

    private static boolean resolveVersion(ObjectNameVersion version) {
        return version.getVersion() >= ObjectNameVersion.v3.getVersion();
    }

    public ServerInterceptor build() {
        HeaderReader<Header> headerReaderV1 = buildHeaderReaderV1(v3);
        HeaderReader<Header> headerReaderV4 = new ServerHeaderReaderV4(name, ServerInterceptorFactory::emptyProperties);
        HeaderReader<Header> headerReader = new ServerHeaderReaderAdaptor(name, headerReaderV1, headerReaderV4);
        return new HeaderPropagationInterceptor(headerReader);
    }

    private HeaderReader<Header> buildHeaderReaderV1(boolean v3) {
        if (v3) {
            return ServerHeaderReaderV1.v3(name, ServerInterceptorFactory::emptyProperties);
        }
        return new ServerHeaderReaderV1(name, ServerInterceptorFactory::emptyProperties);
    }
}
