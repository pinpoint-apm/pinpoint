/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

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

/**
 * @author emeroad
 * @author koo.taejin
 * @author netspider
 * @author hyungil.jeong
 * @author jaehong.kim
 *   - add CHUNK_HEADER
 */
class DefaultTBaseLocator implements TBaseLocator {

    private static final short NETWORK_CHECK = 10;
    private static final Header NETWORK_CHECK_HEADER = createHeader(NETWORK_CHECK);

    private static final short SPAN = 40;
    private static final Header SPAN_HEADER = createHeader(SPAN);

    private static final short AGENT_INFO = 50;
    private static final Header AGENT_INFO_HEADER = createHeader(AGENT_INFO);
    
    private static final short AGENT_STAT = 55;
    private static final Header AGENT_STAT_HEADER = createHeader(AGENT_STAT);
    private static final short AGENT_STAT_BATCH = 56;
    private static final Header AGENT_STAT_BATCH_HEADER = createHeader(AGENT_STAT_BATCH);

    private static final short SPANCHUNK = 70;
    private static final Header SPANCHUNK_HEADER = createHeader(SPANCHUNK);

    private static final short SPANEVENT = 80;
    private static final Header SPANEVENT_HEADER = createHeader(SPANEVENT);
    
    private static final short SQLMETADATA = 300;
    private static final Header SQLMETADATA_HEADER = createHeader(SQLMETADATA);

    private static final short APIMETADATA = 310;
    private static final Header APIMETADATA_HEADER = createHeader(APIMETADATA);

    private static final short RESULT = 320;
    private static final Header RESULT_HEADER = createHeader(RESULT);

    private static final short STRINGMETADATA = 330;
    private static final Header STRINGMETADATA_HEADER = createHeader(STRINGMETADATA);
    
    private static final short CHUNK = 400;
    private static final Header CHUNK_HEADER = createHeader(CHUNK);
    
    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
            case SPAN:
                return new TSpan();
            case AGENT_INFO:
                return new TAgentInfo();
            case AGENT_STAT:
                return new TAgentStat();
            case AGENT_STAT_BATCH:
                return new TAgentStatBatch();
            case SPANCHUNK:
                return new TSpanChunk();
            case SPANEVENT:
                return new TSpanEvent();
            case SQLMETADATA:
                return new TSqlMetaData();
            case APIMETADATA:
                return new TApiMetaData();
            case RESULT:
                return new TResult();
            case STRINGMETADATA:
                return new TStringMetaData();
            case NETWORK_CHECK:
                return new NetworkAvailabilityCheckPacket();
        }
        throw new TException("Unsupported type:" + type);
    }

    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }
        if (tbase instanceof TSpan) {
            return SPAN_HEADER;
        }
        if (tbase instanceof TSpanChunk) {
            return SPANCHUNK_HEADER;
        }
        if (tbase instanceof TSpanEvent) {
            return SPANEVENT_HEADER;
        }
        if (tbase instanceof TAgentInfo) {
            return AGENT_INFO_HEADER;
        }
        if (tbase instanceof TAgentStat) {
            return AGENT_STAT_HEADER;
        }
        if (tbase instanceof TAgentStatBatch) {
            return AGENT_STAT_BATCH_HEADER;
        }
        if (tbase instanceof TSqlMetaData) {
            return SQLMETADATA_HEADER;
        }
        if (tbase instanceof TApiMetaData) {
            return APIMETADATA_HEADER;
        }
        if (tbase instanceof TResult) {
            return RESULT_HEADER;
        }
        if (tbase instanceof TStringMetaData) {
            return STRINGMETADATA_HEADER;
        }
        if (tbase instanceof NetworkAvailabilityCheckPacket) {
            return NETWORK_CHECK_HEADER;
        }
        
        throw new TException("Unsupported Type" + tbase.getClass());
    }

    @Override
    public boolean isSupport(short type) {
        try {
            tBaseLookup(type);
            return true;
        } catch (TException ignore) {
            // skip
        }

        return false;
    }

    @Override
    public boolean isSupport(Class<? extends TBase> clazz) {
        if (clazz.equals(TSpan.class)) {
            return true;
        }
        if (clazz.equals(TSpanChunk.class)) {
            return true;
        }
        if (clazz.equals(TAgentInfo.class)) {
            return true;
        }
        if (clazz.equals(TAgentStat.class)) {
            return true;
        }
        if (clazz.equals(TAgentStatBatch.class)) {
            return true;
        }
        if (clazz.equals(TSqlMetaData.class)) {
            return true;
        }
        if (clazz.equals(TApiMetaData.class)) {
            return true;
        }
        if (clazz.equals(TResult.class)) {
            return true;
        }
        if (clazz.equals(TStringMetaData.class)) {
            return true;
        }
        if (clazz.equals(NetworkAvailabilityCheckPacket.class)) {
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
        return CHUNK_HEADER;
    }

    @Override
    public boolean isChunkHeader(short type) {
        return CHUNK == type;
    }
}
