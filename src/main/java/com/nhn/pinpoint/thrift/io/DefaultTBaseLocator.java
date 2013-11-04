package com.nhn.pinpoint.thrift.io;

import com.nhn.pinpoint.thrift.dto.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

/**
 * @author emeroad
 */
class DefaultTBaseLocator implements TBaseLocator {

    private static final short SPAN = 40;
    private static final Header SPAN_HEADER = createHeader(SPAN);

    private static final short AGENT_INFO = 50;
    private static final Header AGENT_INFO_HEADER = createHeader(AGENT_INFO);
    
    private static final short AGENT_STAT = 55;
    private static final Header AGENT_STAT_HEADER = createHeader(AGENT_STAT);


    private static final short SPANCHUNK = 70;
    private static final Header SPANCHUNK_HEADER = createHeader(SPANCHUNK);


    private static final short SQLMETADATA = 300;
    private static final Header SQLMETADATA_HEADER = createHeader(SQLMETADATA);

    private static final short APIMETADATA = 310;
    private static final Header APIMETADATA_HEADER = createHeader(APIMETADATA);

    private static final short RESULT = 320;
    private static final Header RESULT_HEADER = createHeader(RESULT);

    private static final short STRINGMETADATA = 330;
    private static final Header STRINGMETADATA_HEADER = createHeader(STRINGMETADATA);


    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
            case SPAN:
                return new TSpan();
            case AGENT_INFO:
                return new TAgentInfo();
            case AGENT_STAT:
                return new TAgentStat();
            case SPANCHUNK:
                return new TSpanChunk();
            case SQLMETADATA:
                return new TSqlMetaData();
            case APIMETADATA:
                return new TApiMetaData();
            case RESULT:
                return new TResult();
            case STRINGMETADATA:
                return new TStringMetaData();
        }
        throw new TException("Unsupported type:" + type);
    }

    private short typeLookup(TBase<?, ?> tbase) throws TException {
        if (tbase instanceof TSpan) {
            return SPAN;
        }
        if (tbase instanceof TSpanChunk) {
            return SPANCHUNK;
        }
        if (tbase instanceof TAgentInfo) {
            return AGENT_INFO;
        }
        if (tbase instanceof TAgentStat) {
            return AGENT_STAT;
        }
        if (tbase instanceof TSqlMetaData) {
            return SQLMETADATA;
        }
        if (tbase instanceof TApiMetaData) {
            return APIMETADATA;
        }
        if (tbase instanceof TResult) {
            return RESULT;
        }
        if (tbase instanceof TStringMetaData) {
            return STRINGMETADATA;
        }
        throw new TException("Unsupported Type" + tbase.getClass());
    }

    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }
        short type = typeLookup(tbase);
        switch (type) {
            case SPAN:
                return SPAN_HEADER;
            case AGENT_INFO:
                return AGENT_INFO_HEADER;
            case AGENT_STAT:
                return AGENT_STAT_HEADER;
            case SPANCHUNK:
                return SPANCHUNK_HEADER;
            case SQLMETADATA:
                return SQLMETADATA_HEADER;
            case APIMETADATA:
                return APIMETADATA_HEADER;
            case RESULT:
                return RESULT_HEADER;
            case STRINGMETADATA:
                return STRINGMETADATA_HEADER;
        }
        throw new TException("Unsupported type:" + tbase.getClass());
    }

    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
    }
}
