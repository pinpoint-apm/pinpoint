package com.profiler.common.io;

import com.profiler.common.dto.Header;
import com.profiler.common.dto.thrift.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public class DefaultTBaseLocator implements TBaseLocator {

    private static final short JVM_INFO_THRIFT_DTO = 10;
    private static final Header JVM_INFO_THRIFT_HEADER = createHeader(JVM_INFO_THRIFT_DTO);

    private static final short REQUEST_DATA_LIST_THRIFT_DTO = 20;
    private static final Header REQUEST_DATA_LIST_THRIFT_HEADER = createHeader(REQUEST_DATA_LIST_THRIFT_DTO);

    private static final short REQUEST_THRIFT_DTO = 30;
    private static final Header REQUEST_THRIFT_HEADER = createHeader(REQUEST_THRIFT_DTO);


    private static final short SPAN = 40;
    private static final Header SPAN_HEADER = createHeader(SPAN);

    private static final short AGENT_INFO = 50;
    private static final Header AGENT_INFO_HEADER = createHeader(AGENT_INFO);

    private static final short SUBSPAN = 60;
    private static final Header SUBSPAN_HEADER = createHeader(SUBSPAN);

    private static final short SUBSPANLIST = 70;
    private static final Header SUBSPANLIST_HEADER = createHeader(SUBSPANLIST);


    private static final short SQLMETADATA = 300;
    private static final Header SQLMETADATA_HEADER = createHeader(SQLMETADATA);

    private static final short APIMETADATA = 310;
    private static final Header APIMETADATA_HEADER = createHeader(APIMETADATA);

    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
            case JVM_INFO_THRIFT_DTO:
                return new JVMInfoThriftDTO();
            case REQUEST_DATA_LIST_THRIFT_DTO:
                return new RequestDataListThriftDTO();
            case REQUEST_THRIFT_DTO:
                return new RequestThriftDTO();
            case SPAN:
                return new Span();
            case AGENT_INFO:
                return new AgentInfo();
            case SUBSPAN:
                return new SubSpan();
            case SUBSPANLIST:
                return new SubSpanList();
            case SQLMETADATA:
                return new SqlMetaData();
            case APIMETADATA:
                return new ApiMetaData();
        }
        throw new TException("Unsupported type:" + type);
    }

    private short typeLookup(TBase<?, ?> tbase) throws TException {
        if (tbase instanceof Span) {
            return SPAN;
        }
        if (tbase instanceof SubSpanList) {
            return SUBSPANLIST;
        }
        if (tbase instanceof SubSpan) {
            return SUBSPAN;
        }
        if (tbase instanceof JVMInfoThriftDTO) {
            return JVM_INFO_THRIFT_DTO;
        }
        if (tbase instanceof RequestDataListThriftDTO) {
            return REQUEST_DATA_LIST_THRIFT_DTO;
        }
        if (tbase instanceof RequestThriftDTO) {
            return REQUEST_THRIFT_DTO;
        }
        if (tbase instanceof AgentInfo) {
            return AGENT_INFO;
        }
        if (tbase instanceof SqlMetaData) {
            return SQLMETADATA;
        }
        if (tbase instanceof ApiMetaData) {
            return APIMETADATA;
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
            case REQUEST_DATA_LIST_THRIFT_DTO:
                return REQUEST_DATA_LIST_THRIFT_HEADER;
            case REQUEST_THRIFT_DTO:
                return REQUEST_THRIFT_HEADER;
            case SPAN:
                return SPAN_HEADER;
            case AGENT_INFO:
                return AGENT_INFO_HEADER;
            case SUBSPAN:
                return SUBSPAN_HEADER;
            case SUBSPANLIST:
                return SUBSPANLIST_HEADER;
            case SQLMETADATA:
                return SQLMETADATA_HEADER;
            case APIMETADATA:
                return APIMETADATA_HEADER;
        }
        throw new TException("Unsupported type:" + tbase.getClass());
    }

    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
    }
}
