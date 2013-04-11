package com.profiler;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import com.profiler.sender.DataSender;



/**
 *
 */
public class HeartBitChecker {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private long heartBitInterVal;
    private DataSender dataSender;
    private AgentInfo agentInfo;

    private Thread ioThread;


    public HeartBitChecker(DataSender dataSender, long heartBitInterVal, AgentInfo agentInfo) {
        this.dataSender = dataSender;
        this.heartBitInterVal = heartBitInterVal;
        this.agentInfo = agentInfo;
    }


    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("Send startup information to HIPPO server via " + dataSender.getClass().getSimpleName() + ". agentInfo=" + agentInfo);
        }
        dataSender.send(agentInfo);
        dataSender.send(agentInfo);
        dataSender.send(agentInfo);

        this.ioThread = new Thread(heartBitCommand, "HIPPO-Agent-Heartbeat-Thread");
        this.ioThread.setDaemon(true);
        ioThread.start();
    }


    private Runnable heartBitCommand = new Runnable() {
        @Override
        public void run() {

            if (logger.isInfoEnabled()) {
                logger.info("Starting agent heartbeat. heartbeatInterval:" + heartBitInterVal);
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


    public void close() {
        logger.info("HeartBitChecker stop");
        ioThread.interrupt();
        try {
            ioThread.join(1000*5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
