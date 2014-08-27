package com.nhn.pinpoint.profiler;

import java.util.concurrent.CountDownLatch;

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

public class HeartBitCheckerListener implements FutureListener<ResponseMessage> {

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
	public void onComplete(Future<ResponseMessage> future) {
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
