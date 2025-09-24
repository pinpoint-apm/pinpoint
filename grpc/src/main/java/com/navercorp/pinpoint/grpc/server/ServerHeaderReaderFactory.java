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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import io.grpc.Metadata;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class ServerHeaderReaderFactory {

    private final String name;

    private final Function<Metadata, Map<String, Object>> metadataConverter;

    private final boolean v3;


    public static Map<String, Object> emptyProperties(Metadata headers) {
        return Collections.emptyMap();
    }

    public ServerHeaderReaderFactory(String name, Function<Metadata, Map<String, Object>> metadataConverter, boolean v3) {
        this.name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(metadataConverter, "metadataConverter");

        this.metadataConverter = Objects.requireNonNull(metadataConverter, "metadataConverter");
        this.v3 = v3;
    }

    public HeaderReader<Header> build() {
        HeaderReader<Header> headerReaderV1;
        if (v3) {
            headerReaderV1 = ServerHeaderReaderV1.v3(name, metadataConverter);
        } else {
            headerReaderV1 = new ServerHeaderReaderV1(name, metadataConverter);
        }
        HeaderReader<Header> headerReaderV4 = new ServerHeaderReaderV4(name, metadataConverter);

        return new ServerHeaderReaderAdaptor(name, headerReaderV1, headerReaderV4);
    }


    @Override
    public String toString() {
        return "ServerHeaderReaderFactory{" +
                "name=" + name +
                '}';
    }
}
