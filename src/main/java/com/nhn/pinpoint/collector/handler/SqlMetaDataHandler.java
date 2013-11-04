package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.TSqlMetaData;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.SqlMetaDataDao;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class SqlMetaDataHandler implements RequestResponseHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SqlMetaDataDao sqlMetaDataDao;

	@Override
	public TBase<?, ?> handler(TBase<?, ?> tbase) {
		if (!(tbase instanceof TSqlMetaData)) {
			logger.error("invalid tbase:{}", tbase);
			return null;
		}
		
		TSqlMetaData sqlMetaData = (TSqlMetaData) tbase;
		
		if (logger.isInfoEnabled()) {
			logger.info("Received SqlMetaData:{}", sqlMetaData);
		}
		

        try {
            sqlMetaDataDao.insert(sqlMetaData);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            TResult result = new TResult(false);
            result.setMessage(e.getMessage());
            return result;
        }
        return new TResult(true);
	}
}
