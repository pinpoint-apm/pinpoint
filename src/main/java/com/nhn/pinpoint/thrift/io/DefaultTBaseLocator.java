package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TApiMetaData;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSqlMetaData;
import com.nhn.pinpoint.thrift.dto.TStringMetaData;

/**
 * @author emeroad
 * @author netspider
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
        	case NETWORK_CHECK:
        		return new NetworkAvailabilityCheckPacket();
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

    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }
        if (tbase instanceof NetworkAvailabilityCheckPacket) {
        	return NETWORK_CHECK_HEADER;
        }
        if (tbase instanceof TSpan) {
            return SPAN_HEADER;
        }
        if (tbase instanceof TSpanChunk) {
            return SPANCHUNK_HEADER;
        }
        if (tbase instanceof TAgentInfo) {
            return AGENT_INFO_HEADER;
        }
        if (tbase instanceof TAgentStat) {
            return AGENT_STAT_HEADER;
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
        throw new TException("Unsupported Type" + tbase.getClass());
    }

    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
    }
}
