package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.collector.dao.StringMetaDataDao;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.TStringMetaData;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class StringMetaDataHandler implements RequestResponseHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StringMetaDataDao stringMetaDataDao;

	@Override
	public TBase<?, ?> handleRequest(TBase<?, ?> tbase) {
		if (!(tbase instanceof TStringMetaData)) {
			logger.error("invalid tbase:{}", tbase);
			return null;
		}
		
		TStringMetaData stringMetaData = (TStringMetaData) tbase;
        // api 데이터는 중요한거니 그냥 info로 찍음.
		if (logger.isInfoEnabled()) {
			logger.info("Received StringMetaData={}", stringMetaData);
		}

        try {
            stringMetaDataDao.insert(stringMetaData);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            TResult result = new TResult(false);
            result.setMessage(e.getMessage());
            return result;
        }
        return new TResult(true);
	}
}
