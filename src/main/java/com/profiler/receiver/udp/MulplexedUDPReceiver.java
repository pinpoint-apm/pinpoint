package com.profiler.receiver.udp;

import com.profiler.config.TomcatProfilerReceiverConfig;
import org.apache.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.*;

public class MulplexedUDPReceiver implements DataReceiver {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private ExecutorService worker = Executors.newFixedThreadPool(1024);

    private DatagramSocket udpSocket = null;

    private Thread packetReader = new Thread(MulplexedUDPReceiver.class.getSimpleName()) {
        @Override
        public void run() {
            receive();
        }
    };

    private static final int AcceptedSize = 65507;

    public MulplexedUDPReceiver() {
    }

    long rejectedExecutionCount = 0;

    public void receive() {
        try {
            this.udpSocket = new DatagramSocket(TomcatProfilerReceiverConfig.DEFUALT_PORT);
            System.out.println("ddddd");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (udpSocket != null) {
            System.out.println("Waiting for " + MulplexedUDPReceiver.class.getSimpleName());
            while (true) {
                // TODO 최대 사이즈로 수정필요.

                byte[] buffer = new byte[AcceptedSize];
                try {
//					System.out.println("ReceiveBufferSize="+udpSocket.getReceiveBufferSize());
                    DatagramPacket packet = new DatagramPacket(buffer, AcceptedSize);
                    udpSocket.receive(packet);
                    if (logger.isDebugEnabled()) {
                        logger.debug("DatagramPacket read size:" + packet.getLength());
                    }
                    worker.execute(new MulplexedPacketHandler(packet));
                } catch (RejectedExecutionException ree) {
                    rejectedExecutionCount++;
                    if (rejectedExecutionCount > 1000) {
                        System.out.println("rejectedExecutionCount=1000");
                        rejectedExecutionCount = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("There is problem with making UDP Socket connection.");
        }
    }


    @Override
    public void start() {
        this.packetReader.start();
    }

    @Override
    public void shutdown() {
        // TODO 가능한 gracefull shutdown 구현필요.
//        this.udpSocket.close();
//        this.worker.shutdown();
//        try {
//            this.worker.awaitTermination(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

    }
}
