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
package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.util.BodyFactory;
import com.navercorp.pinpoint.io.util.HeaderFactory;
import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.io.util.TypeLocatorBuilder;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.thrift.TBase;

/**
 * @author minwoo.jung
 */
public class FlinkTBaseLocator {

    public static final short AGENT_STAT_BATCH = 1000;

    private final byte version;
    private final TypeLocator<TBase<?, ?>> typeLocator;

    public FlinkTBaseLocator(byte version) {
        if (version != HeaderV1.VERSION && version != HeaderV2.VERSION) {
            throw new IllegalArgumentException(String.format("could not select match header version. : 0x%02X", version));
        }
        this.version = version;
        this.typeLocator = newTypeLocator();
    }

    private TypeLocator<TBase<?, ?>> newTypeLocator() {
        HeaderFactory headerFactory = new FlinkHeaderFactory();
        TypeLocatorBuilder<TBase<?, ?>> typeLocatorBuilder = new TypeLocatorBuilder<TBase<?, ?>>(headerFactory);
        typeLocatorBuilder.addBodyFactory(AGENT_STAT_BATCH, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TFAgentStatBatch();
            }
        });

        return typeLocatorBuilder.build();
    }

    public TypeLocator<TBase<?, ?>> getTypeLocator() {
        return typeLocator;
    }

    public class FlinkHeaderFactory implements HeaderFactory {
        @Override
        public Header newHeader(short type) {
            return createHeader(type);
        }

        private Header createHeader(short type) {
            if (version == HeaderV1.VERSION) {
                return createHeaderV1(type);
            } else if (version == HeaderV2.VERSION) {
                return createHeaderV2(type);
            }

            throw new IllegalArgumentException("unsupported Header version : " + version);
        }

        private Header createHeaderV1(short type) {
            return new HeaderV1(type);
        }

        private Header createHeaderV2(short type) {
            return new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type);
        }
    };

}
