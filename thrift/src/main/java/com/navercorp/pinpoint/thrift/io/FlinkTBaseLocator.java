/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderDataGenerator;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author minwoo.jung
 */
public class FlinkTBaseLocator implements TBaseLocator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final short AGENT_STAT_BATCH = 1000;

    private final byte version;
    private final HeaderDataGenerator headerDataGenerator;

    public FlinkTBaseLocator(byte version, HeaderDataGenerator headerDataGenerator) {
        if (version != HeaderV1.VERSION && version != HeaderV2.VERSION) {
            throw new IllegalArgumentException(String.format("could not select match header version. : 0x%02X", version));
        }
        this.version = version;

        if (headerDataGenerator == null) {
            throw new NullPointerException("headerDataGenerator must not be null.");
        }
        this.headerDataGenerator = headerDataGenerator;
    }

    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
            case AGENT_STAT_BATCH:
                return new TFAgentStatBatch();
        }
        throw new TException("Unsupported type:" + type);
    }

    @Override
    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase instanceof TFAgentStatBatch) {
            return createHeader(AGENT_STAT_BATCH);
        }

        throw new TException("Unsupported Type" + tbase.getClass());
    }

    @Override
    public boolean isSupport(short type) {
        try {
            tBaseLookup(type);
            return true;
        } catch (TException ignore) {
            logger.warn("{} is not support type", type);
        }

        return false;
    }

    @Override
    public boolean isSupport(Class<? extends TBase> clazz) {
        if (clazz.equals(TFAgentStatBatch.class)) {
            return true;
        }

        return false;
    }

    private Header createHeader(short type) {
        if (version == HeaderV1.VERSION) {
            return createHeaderv1(type);
        } else if (version == HeaderV2.VERSION) {
            return createHeaderv2(type);
        }

        throw new IllegalArgumentException("unsupported Header version : " + version);
    }

    private Header createHeaderv1(short type) {
        return new HeaderV1(type);
    }

    private Header createHeaderv2(short type) {
        Map<String, String> data = headerDataGenerator.generate();
        return new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type, data);
    }

    @Override
    public Header getChunkHeader() {
        return null;
    }

    @Override
    public boolean isChunkHeader(short type) {
        return false;
    }
}
