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
import com.navercorp.pinpoint.grpc.server.ServerHeaderReaderFactory;
import io.grpc.ServerInterceptor;

import java.util.Objects;

public class ServerInterceptorFactory {

    private final String name;
    private boolean v3;

    public ServerInterceptorFactory(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public void applyVersion(ObjectNameVersion version) {
        if (ObjectNameVersion.v3 == version || ObjectNameVersion.v4 == version) {
            enableV3();
        }
    }

    public void enableV3() {
        this.v3 = true;
    }

    public ServerInterceptor build() {
        ServerHeaderReaderFactory serverHeaderReaderFactory = new ServerHeaderReaderFactory(name, ServerHeaderReaderFactory::emptyProperties, v3);
        HeaderReader<Header> headerReader = serverHeaderReaderFactory.build();
        return new HeaderPropagationInterceptor(headerReader);
    }
}
