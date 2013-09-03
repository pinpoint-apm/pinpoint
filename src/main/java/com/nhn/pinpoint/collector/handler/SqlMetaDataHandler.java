package com.nhn.pinpoint.collector.handler;

import java.net.DatagramPacket;

import com.nhn.pinpoint.thrift.dto.Result;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.thrift.dto.SqlMetaData;
import com.nhn.pinpoint.collector.dao.SqlMetaDataDao;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class SqlMetaDataHandler implements RequestResponseHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SqlMetaDataDao sqlMetaDataDao;

	@Override
	public TBase<?, ?> handler(TBase<?, ?> tbase) {
		if (!(tbase instanceof SqlMetaData)) {
			logger.error("invalid tbase:{}", tbase);
			return null;
		}
		
		SqlMetaData sqlMetaData = (SqlMetaData) tbase;
		
		if (logger.isInfoEnabled()) {
			logger.info("Received SqlMetaData:{}", sqlMetaData);
		}
		

        try {
            sqlMetaDataDao.insert(sqlMetaData);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            Result result = new Result(false);
            result.setMessage(e.getMessage());
            return result;
        }
        return new Result(true);
	}
}
