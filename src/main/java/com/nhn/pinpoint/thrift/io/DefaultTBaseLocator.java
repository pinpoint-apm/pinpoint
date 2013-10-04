package com.nhn.pinpoint.thrift.io;

import com.nhn.pinpoint.thrift.dto.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

class DefaultTBaseLocator implements TBaseLocator {

    private static final short JVM_INFO_THRIFT_DTO = 10;
    private static final Header JVM_INFO_THRIFT_HEADER = createHeader(JVM_INFO_THRIFT_DTO);


    private static final short SPAN = 40;
    private static final Header SPAN_HEADER = createHeader(SPAN);

    private static final short AGENT_INFO = 50;
    private static final Header AGENT_INFO_HEADER = createHeader(AGENT_INFO);
    
    private static final short AGENT_STAT = 55;
    private static final Header AGENT_STAT_HEADER = createHeader(AGENT_STAT);

    private static final short SPANEVENT = 60;
    private static final Header SPANEVENT_HEADER = createHeader(SPANEVENT);

    private static final short SPANCHUNK = 70;
    private static final Header SPANCHUNK_HEADER = createHeader(SPANCHUNK);


    private static final short SQLMETADATA = 300;
    private static final Header SQLMETADATA_HEADER = createHeader(SQLMETADATA);

    private static final short APIMETADATA = 310;
    private static final Header APIMETADATA_HEADER = createHeader(APIMETADATA);

    private static final short RESULT = 320;
    private static final Header RESULT_HEADER = createHeader(RESULT);

    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
            case JVM_INFO_THRIFT_DTO:
                return new JVMInfoThriftDTO();
            case SPAN:
                return new Span();
            case AGENT_INFO:
                return new AgentInfo();
            case AGENT_STAT:
                return new AgentStat();
            case SPANEVENT:
                return new SpanEvent();
            case SPANCHUNK:
                return new SpanChunk();
            case SQLMETADATA:
                return new SqlMetaData();
            case APIMETADATA:
                return new ApiMetaData();
            case RESULT:
                return new Result();
        }
        throw new TException("Unsupported type:" + type);
    }

    private short typeLookup(TBase<?, ?> tbase) throws TException {
        if (tbase instanceof Span) {
            return SPAN;
        }
        if (tbase instanceof SpanChunk) {
            return SPANCHUNK;
        }
        if (tbase instanceof SpanEvent) {
            return SPANEVENT;
        }
        if (tbase instanceof JVMInfoThriftDTO) {
            return JVM_INFO_THRIFT_DTO;
        }
        if (tbase instanceof AgentInfo) {
            return AGENT_INFO;
        }
        if (tbase instanceof AgentStat) {
            return AGENT_STAT;
        }
        if (tbase instanceof SqlMetaData) {
            return SQLMETADATA;
        }
        if (tbase instanceof ApiMetaData) {
            return APIMETADATA;
        }
        if (tbase instanceof Result) {
            return RESULT;
        }
        throw new TException("Unsupported Type" + tbase.getClass());
    }

    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }
        short type = typeLookup(tbase);
        switch (type) {
            case JVM_INFO_THRIFT_DTO:
                return JVM_INFO_THRIFT_HEADER;
            case SPAN:
                return SPAN_HEADER;
            case AGENT_INFO:
                return AGENT_INFO_HEADER;
            case AGENT_STAT:
                return AGENT_STAT_HEADER;
            case SPANEVENT:
                return SPANEVENT_HEADER;
            case SPANCHUNK:
                return SPANCHUNK_HEADER;
            case SQLMETADATA:
                return SQLMETADATA_HEADER;
            case APIMETADATA:
                return APIMETADATA_HEADER;
            case RESULT:
                return RESULT_HEADER;
        }
        throw new TException("Unsupported type:" + tbase.getClass());
    }

    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
    }
}
