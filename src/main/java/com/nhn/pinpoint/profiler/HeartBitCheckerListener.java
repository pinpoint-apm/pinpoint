package com.nhn.pinpoint.profiler;

import java.util.concurrent.CountDownLatch;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.profiler.util.ClassUtils;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerDesFactory;

public class HeartBitCheckerListener<T> implements FutureListener<T> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final HeartBitStateContext state;
	private final CountDownLatch latch;
	private final long createTimeMillis;

	public HeartBitCheckerListener(HeartBitStateContext state, CountDownLatch latch) {
		this.state = state;
		this.latch = latch;
		this.createTimeMillis = System.currentTimeMillis();
	}

	// Latch 타이밍 중요함 잘못 걸면 문제한 대기할수 있음

	@Override
	public void onComplete(Future future) {
		try {
			if (future != null && future.isSuccess()) {
				TBase tbase = deserialize(future);
				if (tbase instanceof TResult) {
					TResult result = (TResult) tbase;
					if (result.isSuccess()) {
						logger.debug("result success");
						state.changeStateToNeedNotRequest(createTimeMillis);
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
		} finally {
			latch.countDown();
		}
		state.changeStateToNeedRequest(System.currentTimeMillis());
	}

	private TBase<?, ?> deserialize(Future future) {
		Object result = future.getResult();

		if (ClassUtils.isAssignableValue(ResponseMessage.class, result)) {
			ResponseMessage responseMessage = (ResponseMessage) result;

			HeaderTBaseDeserializer deserializer = HeaderTBaseSerDesFactory.getDeserializer();
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
		return null;
	}

}
