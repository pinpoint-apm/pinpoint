package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.profiler.sender.DataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;


/**
 * @author emeroad
 */
public class HeartBitChecker {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ThreadFactory THREAD_FACTORY = new PinpointThreadFactory("Pinpoint-Agent-Heartbeat-Thread", true);
    private long heartBitInterVal;
    private DataSender dataSender;
    private TAgentInfo agentInfo;

    private Thread ioThread;


    public HeartBitChecker(DataSender dataSender, long heartBitInterVal, TAgentInfo agentInfo) {
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
        if (logger.isInfoEnabled()) {
            logger.info("Send startup information to Pinpoint server via {}. agentInfo={}", dataSender.getClass().getSimpleName(), agentInfo);
        }
        // agent내의 공용타이머를 생성하고 자체 thread를 대체 할것.
        dataSender.send(agentInfo);
        this.ioThread = THREAD_FACTORY.newThread(heartBitCommand);
        ioThread.start();
    }


    private Runnable heartBitCommand = new Runnable() {
        @Override
        public void run() {

            if (logger.isInfoEnabled()) {
                logger.info("Starting agent heartbeat. heartbeatInterval:{}", heartBitInterVal);
            }
            while (true) {
                dataSender.send(agentInfo);

                // TODO 정밀한 시간계산 없이 일단 그냥 interval 단위로 보냄.
                try {
                    Thread.sleep(heartBitInterVal);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            logger.info("HeartBitChecker ioThread stopped.");
        }
    };


    public void stop() {
        logger.info("HeartBitChecker stop");
        ioThread.interrupt();
        try {
            ioThread.join(1000 * 5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
