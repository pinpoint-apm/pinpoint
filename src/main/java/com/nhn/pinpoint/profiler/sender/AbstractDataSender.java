package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.profiler.sender.message.PinpointMessage;

/**
 * 
 * @author koo.taejin
 */
public abstract class AbstractDataSender implements DataSender {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	abstract protected void sendPacket(PinpointMessage dto);	
	
    protected void sendPacketN(Collection<PinpointMessage> messageList) {
    	// 자체적인 List를 사용하고 있어서 toArray(T[] array] 사용이 불가능 ㅠ_ㅠ
        Object[] dataList = messageList.toArray();
        
		// 일단 single thread에서 하는거라 구지 복사 안해도 될것 같음.
		// Object[] copy = Arrays.copyOf(original, original.length);

		// for (Object data : dataList) {
		// 이렇게 바꾸지 말것. copy해서 return 하는게 아니라 항상 max치가 나옴.
		final int size = messageList.size();
		for (int i = 0; i < size; i++) {
			try {
				sendPacket((PinpointMessage) dataList[i]);
			} catch (Throwable th) {
				logger.warn("Unexpected Error. Cause:{}", th.getMessage(), th);
			}
		}
	}

	protected AsyncQueueingExecutor<PinpointMessage> createAsyncQueueingExecutor(int queueSize, String executorName) {
        final AsyncQueueingExecutor<PinpointMessage> executor = new AsyncQueueingExecutor<PinpointMessage>(queueSize, executorName);
        executor.setListener(new AsyncQueueingExecutorListener<PinpointMessage>() {
            @Override
            public void execute(Collection<PinpointMessage> messageList) {
                sendPacketN(messageList);
            }

            @Override
            public void execute(PinpointMessage message) {
                sendPacket(message);
            }
        });
        return executor;
    }

}
