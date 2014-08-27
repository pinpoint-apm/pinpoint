package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.web.service.map.AcceptApplication;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class HostApplicationMapperVer2 implements RowMapper<List<AcceptApplication>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<AcceptApplication> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
//       readRowKey(result.getRow());

        final List<AcceptApplication> acceptApplicationList = new ArrayList<AcceptApplication>(result.size());
        for (KeyValue kv : result.raw()) {
            AcceptApplication acceptedApplication = createAcceptedApplication(kv.getQualifier());
            acceptApplicationList.add(acceptedApplication);
        }
		return acceptApplicationList;
	}

//    private void readRowKey(byte[] rowKey) {
//        final Buffer rowKeyBuffer= new FixedBuffer(rowKey);
//        final String parentApplicationName = rowKeyBuffer.readPadStringAndRightTrim(HBaseTables.APPLICATION_NAME_MAX_LEN);
//        final short parentApplicationServiceType = rowKeyBuffer.readShort();
//        final long timeSlot = TimeUtils.recoveryTimeMillis(rowKeyBuffer.readLong());
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("parentApplicationName:{}/{} time:{}", parentApplicationName, parentApplicationServiceType, timeSlot);
//        }
//    }

    private AcceptApplication createAcceptedApplication(byte[] qualifier) {
        Buffer reader = new FixedBuffer(qualifier);
        String host = reader.readPrefixedString();
        String bindApplicationName = reader.readPrefixedString();
        short bindServiceType = reader.readShort();
        return new AcceptApplication(host, bindApplicationName, bindServiceType);
    }
}
