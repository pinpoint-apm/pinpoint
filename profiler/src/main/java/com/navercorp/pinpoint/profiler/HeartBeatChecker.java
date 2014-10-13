package com.nhn.pinpoint.profiler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketReconnectEventListener;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;


/**
 * @author emeroad
 * @author koo.taejin
 */
public class HeartBeatChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatChecker.class);

    private static final ThreadFactory THREAD_FACTORY = new PinpointThreadFactory("Pinpoint-Agent-Heartbeat-Thread", true);
    
	private final HeartBitStateContext heartBitState = new HeartBitStateContext();

	// FIXME 디폴트 타임아웃이 3000임 이게 Constants로 빠져있지 않아서 혹 타임아웃 시간 변경될 경우 수정 필요
	private static final long WAIT_LATCH_WAIT_MILLIS = 3000L + 1000L;
	
    private long heartBitInterVal;
    private EnhancedDataSender dataSender;
    private TAgentInfo agentInfo;

    private Thread ioThread;

    public HeartBeatChecker(EnhancedDataSender dataSender, long heartBitInterVal, TAgentInfo agentInfo) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (agentInfo == null) {
            throw new NullPointerException("agentInfo must not be null");
        }
        this.dataSender = dataSender;
        this.heartBitInterVal = heartBitInterVal;
        this.agentInfo = agentInfo;
    }

    public void start() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Send startup information to Pinpoint server via {}. agentInfo={}", dataSender.getClass().getSimpleName(), agentInfo);
        }

        // start 메소드에서는 둘간의 우선순위 신경쓸 필요없음. 
        this.heartBitState.changeStateToNeedRequest(System.currentTimeMillis());
        this.dataSender.addReconnectEventListener(new ReconnectEventListener(heartBitState));
        
        this.ioThread = THREAD_FACTORY.newThread(heartBitCommand);
        this.ioThread.start();
    }

    private Runnable heartBitCommand = new Runnable() {
        @Override
        public void run() {

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Starting agent heartbeat. heartbeatInterval:{}", heartBitInterVal);
            }
            while (true) {
            	if (heartBitState.needRequest()) {
    				CountDownLatch latch = new CountDownLatch(1);
    				// request timeout이 3000기 때문에 latch.await()를 그냥 걸어도됨
    				dataSender.request(agentInfo, new HeartBitCheckerListener(heartBitState, latch));

    				try {
    					boolean awaitSuccess = latch.await(WAIT_LATCH_WAIT_MILLIS, TimeUnit.MILLISECONDS);
    					if (!awaitSuccess) {
    						heartBitState.changeStateToNeedRequest(System.currentTimeMillis());
    					}
    				} catch (InterruptedException e) {
    					Thread.currentThread().interrupt();
    					break;
    				}
            	}

                // TODO 정밀한 시간계산 없이 일단 그냥 interval 단위로 보냄.
                try {
                    Thread.sleep(heartBitInterVal);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            LOGGER.info("HeartBitChecker ioThread stopped.");
        }
    };


    public void stop() {
        LOGGER.info("HeartBitChecker stop");
    	heartBitState.changeStateToFinish();

        ioThread.interrupt();
        try {
            ioThread.join(1000 * 5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static class ReconnectEventListener implements PinpointSocketReconnectEventListener {

    	private final HeartBitStateContext heartBitState;
    	
    	public ReconnectEventListener(HeartBitStateContext heartBitState) {
    		this.heartBitState = heartBitState;
		}
    	
		@Override
		public void reconnectPerformed(PinpointSocket socket) {
			LOGGER.info("Reconnect Performed (Socket = {})", socket);
			this.heartBitState.changeStateToNeedRequest(System.currentTimeMillis());
		}
    }
    
}
