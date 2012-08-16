package com.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.dto.Header;
import com.profiler.util.DefaultTBaseLocator;
import com.profiler.util.HeaderTBaseSerializer;
import com.profiler.util.TBaseLocator;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.profiler.config.TomcatProfilerConfig;


/**
 * @author netspider
 */
public class DataSender extends Thread {

    private final Logger logger = Logger.getLogger(DataSender.class.getName());

    private final LinkedBlockingQueue<TBase<?, ?>> addedQueue = new LinkedBlockingQueue<TBase<?, ?>>(4096);

    private final InetSocketAddress serverAddress = new InetSocketAddress(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.DEFUALT_PORT);

    private DatagramSocket udpSocket = null;
    private TBaseLocator locator = new DefaultTBaseLocator();
    // 주의 single thread용임
    private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

    private DataSender() {
        udpSocket = createSocket();
        setName("HIPPO-DataSender");
        setDaemon(true);
        start();
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

    public boolean addDataToSend(TBase<?, ?> data) {
        // TODO: addedQueue가 full일 때 IllegalStateException처리.
        return addedQueue.add(data);
    }


    // TODO: sender thread가 한 개로 충분한가.
    public void run() {
        while (true) {
            try {
                TBase<?, ?> dto = take();
                if (dto == null) {
                    continue;
                }
                send(dto);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unexpected Error", e);
            }
        }
    }

    private void send(TBase<?, ?> dto) {
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
            return addedQueue.poll(5, TimeUnit.SECONDS);
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
