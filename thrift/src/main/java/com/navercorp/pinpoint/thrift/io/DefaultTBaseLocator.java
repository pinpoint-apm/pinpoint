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

import com.navercorp.pinpoint.io.util.BodyFactory;
import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.io.util.TypeLocatorBuilder;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;

import org.apache.thrift.TBase;

/**
 * @author emeroad
 * @author koo.taejin
 * @author netspider
 * @author hyungil.jeong
 * @author jaehong.kim
 *   - add CHUNK_HEADER
 */
public class DefaultTBaseLocator {

    public static final short NETWORK_CHECK = 10;

    // grpc ping
    public static final short PING = 12;

    public static final short SPAN = 40;

    public static final short AGENT_INFO = 50;

    public static final short AGENT_STAT = 55;

    public static final short AGENT_STAT_BATCH = 56;

    public static final short SPANCHUNK = 70;

    public static final short SPANEVENT = 80;

    public static final short SQLMETADATA = 300;

    public static final short APIMETADATA = 310;

    public static final short RESULT = 320;

    public static final short STRINGMETADATA = 330;

    public static final short CHUNK = 400;

    private static final TypeLocator<TBase<?, ?>> typeLocator = build();

    public static TypeLocator<TBase<?, ?>>build() {

        TypeLocatorBuilder<TBase<?, ?>> builder = new TypeLocatorBuilder<TBase<?, ?>>();
        addBodyFactory(builder);
        return builder.build();
    }

    public static void addBodyFactory(TypeLocatorBuilder<TBase<?, ?>> builder) {
        builder.addBodyFactory(SPAN, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpan();
            }
        });

        builder.addBodyFactory(AGENT_INFO, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TAgentInfo();
            }
        });

        builder.addBodyFactory(AGENT_STAT, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TAgentStat();
            }
        });

        builder.addBodyFactory(AGENT_STAT_BATCH, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TAgentStatBatch();
            }

        });

        builder.addBodyFactory(SPANCHUNK, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpanChunk();
            }

        });

        builder.addBodyFactory(SPANEVENT, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSpanEvent();
            }

        });

        builder.addBodyFactory(SQLMETADATA, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TSqlMetaData();
            }

        });

        builder.addBodyFactory(APIMETADATA, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TApiMetaData();
            }
        });


        builder.addBodyFactory(RESULT, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TResult();
            }

        });

        builder.addBodyFactory(STRINGMETADATA, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new TStringMetaData();
            }
        });

        builder.addBodyFactory(NETWORK_CHECK, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return new NetworkAvailabilityCheckPacket();
            }
        });

        builder.addBodyFactory(CHUNK, new BodyFactory<TBase<?, ?>>() {
            @Override
            public TBase<?, ?> getObject() {
                return null;
            }
        });
    }

    public static TypeLocator<TBase<?, ?>> getTypeLocator() {
        return typeLocator;
    }
}
