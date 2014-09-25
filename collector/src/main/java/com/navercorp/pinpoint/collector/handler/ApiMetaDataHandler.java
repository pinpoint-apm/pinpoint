package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.thrift.dto.TApiMetaData;
import com.nhn.pinpoint.collector.dao.ApiMetaDataDao;
import com.nhn.pinpoint.thrift.dto.TResult;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class ApiMetaDataHandler implements RequestResponseHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApiMetaDataDao sqlMetaDataDao;

	@Override
	public TBase<?, ?> handleRequest(TBase<?, ?> tbase) {
		if (!(tbase instanceof TApiMetaData)) {
			logger.error("invalid tbase:{}", tbase);
			return null;
		}
		
		TApiMetaData apiMetaData = (TApiMetaData) tbase;
        // api 데이터는 중요한거니 그냥 info로 찍음.
		if (logger.isInfoEnabled()) {
			logger.info("Received ApiMetaData={}", apiMetaData);
		}

        try {
            sqlMetaDataDao.insert(apiMetaData);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            TResult result = new TResult(false);
            result.setMessage(e.getMessage());
            return result;
        }
        return new TResult(true);
	}
}
