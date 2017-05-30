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

import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author minwoo.jung
 */
public class FlinkTBaseLocator implements TBaseLocator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final short AGENT_STAT_BATCH = 1000;
    private static final Header AGENT_STAT_BATCH_HEADER = createHeader(AGENT_STAT_BATCH);

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
            return AGENT_STAT_BATCH_HEADER;
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

    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
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
