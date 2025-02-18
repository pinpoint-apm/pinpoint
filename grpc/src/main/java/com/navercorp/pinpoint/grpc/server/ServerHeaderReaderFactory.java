/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.grpc.protocol.ProtocolVersion;
import com.navercorp.pinpoint.grpc.protocol.ProtocolVersionParser;
import io.grpc.Metadata;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class ServerHeaderReaderFactory implements HeaderReader<Header> {

    private final ProtocolVersionParser protocolVersionParser = new ProtocolVersionParser();

    private final String name;
    private final HeaderReader<Header> headerReaderV1;
    private final HeaderReader<Header> headerReaderV4;

    public ServerHeaderReaderFactory(String name) {
        this(name, ServerHeaderReaderFactory::emptyProperties);
    }

    public static Map<String, Object> emptyProperties(Metadata headers) {
        return Collections.emptyMap();
    }

    public ServerHeaderReaderFactory(String name, Function<Metadata, Map<String, Object>> metadataConverter) {
        this.name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(metadataConverter, "metadataConverter");

        this.headerReaderV1 = new ServerHeaderReaderV1(name, metadataConverter);
        this.headerReaderV4 = new ServerHeaderReaderV4(name, metadataConverter);
    }

    @Override
    public Header extract(Metadata headers) {
        final String protocolVersion = headers.get(Header.PROTOCOL_VERSION_NAME_KEY);
        final ProtocolVersion version = protocolVersionParser.parse(protocolVersion);
        if (version == ProtocolVersion.V1) {
            return headerReaderV1.extract(headers);
        } else if (version == ProtocolVersion.V4) {
            return headerReaderV4.extract(headers);
        }
        return headerReaderV1.extract(headers);
    }

    @Override
    public String toString() {
        return "ServerHeaderReaderFactory{" +
                "name=" + name +
                '}';
    }
}
