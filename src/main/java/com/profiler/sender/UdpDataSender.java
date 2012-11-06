package com.profiler.sender;

import com.profiler.common.dto.Header;
import com.profiler.common.util.DefaultTBaseLocator;
import com.profiler.common.util.HeaderTBaseSerializer;
import com.profiler.common.util.TBaseLocator;
import com.profiler.config.ProfilerConfig;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public class UdpDataSender implements DataSender, Runnable {

    private final Logger logger = Logger.getLogger(UdpDataSender.class.getName());

    private final LinkedBlockingQueue<TBase<?, ?>> queue = new LinkedBlockingQueue<TBase<?, ?>>(1024);

    private final InetSocketAddress serverAddress = new InetSocketAddress(ProfilerConfig.SERVER_IP, ProfilerConfig.SERVER_UDP_PORT);

    private DatagramSocket udpSocket = null;
    private TBaseLocator locator = new DefaultTBaseLocator();
    // 주의 single thread용임
    private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

    private boolean started = false;
    private Object stopLock = new Object();

    private Thread ioThread;

    private static class SingletonHolder {
        public static final UdpDataSender INSTANCE = new UdpDataSender();
    }


    public static UdpDataSender getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private UdpDataSender() {
        udpSocket = createSocket();
        ioThread = new Thread(this);
        ioThread.setName("HIPPO-DataSender");
        ioThread.setDaemon(true);
        ioThread.start();
        started = true;
    }

    private DatagramSocket createSocket() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(1000 * 5);
            datagramSocket.connect(serverAddress);
            return datagramSocket;
        } catch (SocketException e) {
            return null;
        }
    }

    public boolean send(TBase<?, ?> data) {
        if (!started) {
            return false;
        }
        // TODO: addedQueue가 full일 때 IllegalStateException처리.
        return queue.offer(data);
    }

    @Override
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        // io thread 안전 종료. queue 비우기.
    }

    // TODO: sender thread가 한 개로 충분한가.
    public void run() {
        while (true) {
            try {
                TBase<?, ?> dto = take();
                if (dto == null) {
                    continue;
                }
                send0(dto);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unexpected Error", e);
            }
        }
    }

    private void send0(TBase<?, ?> dto) {
        byte[] sendData = serialize(dto);
        if (sendData == null) {
            return;
        }
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length);
        if (udpSocket == null) {
            // socket생성에 문제가 있으면 재생성?
            udpSocket = createSocket();
        }
        if (udpSocket != null) {
            try {
                udpSocket.send(packet);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Data sent. " + dto);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "packet send error " + dto, e);
            }
        }
    }

    // TODO: addedqueue에서 bulk로 drain
    private TBase<?, ?> take() {
        try {
            return queue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    private byte[] serialize(TBase<?, ?> dto) {
        Header header = createHeader(dto);
        try {
            return serializer.serialize(header, dto);
        } catch (TException e) {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "Serialize fail:" + dto, e);
            }
            return null;
        }
    }

    private Header createHeader(TBase<?, ?> dto) {
        short type = locator.typeLookup(dto);
        Header header = new Header();
        header.setType(type);
        return header;
    }
}
