package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.util.SerializationUtils;

/**
 * 
 * @author koo.taejin
 */
public abstract class AbstractDataSender implements DataSender {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	abstract protected void sendPacket(Object dto);	
	
    protected void sendPacketN(Collection<Object> messageList) {
    	// 자체적인 List를 사용하고 있어서 toArray(T[] array] 사용이 불가능 ㅠ_ㅠ
        Object[] dataList = messageList.toArray();
        
		// 일단 single thread에서 하는거라 구지 복사 안해도 될것 같음.
		// Object[] copy = Arrays.copyOf(original, original.length);

		// for (Object data : dataList) {
		// 이렇게 바꾸지 말것. copy해서 return 하는게 아니라 항상 max치가 나옴.
		final int size = messageList.size();
		for (int i = 0; i < size; i++) {
			try {
				sendPacket(dataList[i]);
			} catch (Throwable th) {
				logger.warn("Unexpected Error. Cause:{}", th.getMessage(), th);
			}
		}
	}

	protected AsyncQueueingExecutor<Object> createAsyncQueueingExecutor(int queueSize, String executorName) {
        final AsyncQueueingExecutor<Object> executor = new AsyncQueueingExecutor<Object>(queueSize, executorName);
        executor.setListener(new AsyncQueueingExecutorListener<Object>() {
            @Override
            public void execute(Collection<Object> messageList) {
                sendPacketN(messageList);
            }

            @Override
            public void execute(Object message) {
                sendPacket(message);
            }
        });
        return executor;
    }

	protected byte[] serialize(HeaderTBaseSerializer serializer, TBase tBase) {
		return SerializationUtils.serialize(tBase, serializer, null);
	}
	
	protected TBase<?, ?> deserialize(HeaderTBaseDeserializer deserializer, ResponseMessage responseMessage) {
		byte[] message = responseMessage.getMessage();
		return SerializationUtils.deserialize(message, deserializer, null);
	}
	
	protected static class RequestMarker {
		private final TBase tBase;
		private final int retryCount;
		private final FutureListener futureListener;

		protected RequestMarker(TBase tBase, int retryCount) {
			this.tBase = tBase;
			this.retryCount = retryCount;
			this.futureListener = null;
		}
		
		protected RequestMarker(TBase tBase, FutureListener futureListener) {
			this.tBase = tBase;
			this.retryCount = 3;
			this.futureListener = futureListener;
		}

		protected TBase getTBase() {
			return tBase;
		}

		protected int getRetryCount() {
			return retryCount;
		}
		
		protected FutureListener getFutureListener() {
			return futureListener;
		}
	}
	
}
