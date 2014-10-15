package com.nhn.pinpoint.profiler;

import java.util.concurrent.atomic.AtomicBoolean;

import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;

public class AgentInfoSenderListener implements FutureListener<ResponseMessage> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AtomicBoolean isSuccessful;
	
	public AgentInfoSenderListener(AtomicBoolean isSuccessful) {
	    this.isSuccessful = isSuccessful;
	}

	@Override
	public void onComplete(Future<ResponseMessage> future) {
		try {
			if (future != null && future.isSuccess()) {
				TBase<?, ?> tbase = deserialize(future);
				if (tbase instanceof TResult) {
					TResult result = (TResult) tbase;
					if (result.isSuccess()) {
						logger.debug("result success");
						this.isSuccessful.set(true);
						return;
					} else {
						logger.warn("request fail. Caused:{}", result.getMessage());
					}
				} else {
					logger.warn("Invalid Class. {}", tbase);
				}
			}
		} catch(Exception e) {
			logger.warn("request fail. caused:{}", e.getMessage());
		}
	}

	private TBase<?, ?> deserialize(Future<ResponseMessage> future) {
        final ResponseMessage responseMessage = future.getResult();

        // TODO theradlocalcache로 변경해야 되는지 검토 자주 생성이 될수 있는 객체라서 life cycle이 상이함.
        HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
        byte[] message = responseMessage.getMessage();
        // caching해야 될려나?
        try {
            return deserializer.deserialize(message);
        } catch (TException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Deserialize fail. Caused:{}", e.getMessage(), e);
            }
            return null;
        }
	}

}
