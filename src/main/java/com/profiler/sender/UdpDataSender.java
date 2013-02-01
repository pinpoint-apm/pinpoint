package com.profiler.sender;

import com.profiler.common.dto.Header;
import com.profiler.common.util.DefaultTBaseLocator;
import com.profiler.common.util.HeaderTBaseSerializer;
import com.profiler.common.util.TBaseLocator;
import com.profiler.context.Thriftable;
import com.profiler.util.Assert;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public class UdpDataSender implements DataSender, Runnable {

    private final Logger logger = Logger.getLogger(UdpDataSender.class.getName());

    private final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(1024);

    private int maxDrainSize = 10;
    // 주의 single thread용임
    private List<Object> drain = new ArrayList<Object>(maxDrainSize);

    private DatagramSocket udpSocket = null;
    private Thread ioThread;

    private TBaseLocator locator = new DefaultTBaseLocator();
    // 주의 single thread용임
    private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();


    private AtomicBoolean started = new AtomicBoolean();
    private Object stopLock = new Object();

    public UdpDataSender(String host, int port) {
        Assert.notNull(host, "host must not be null");

        // Socket 생성에 에러가 발생하면 Agent start가 안되게 변경.
        this.udpSocket = createSocket(host, port);

        this.ioThread = createIoThread();

        this.started.set(true);
    }

    private Thread createIoThread() {
        Thread thread = new Thread(this);
        thread.setName("HIPPO-UdpDataSender-IoThread");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private DatagramSocket createSocket(String host, int port) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(1000 * 5);

            InetSocketAddress serverAddress = new InetSocketAddress(host, port);
            datagramSocket.connect(serverAddress);
            return datagramSocket;
        } catch (SocketException e) {
            throw new IllegalStateException("DataramSocket create fail. Cause" + e.getMessage(), e);
        }
    }

    public boolean send(TBase<?, ?> data) {
        return putQueue(data);
    }

    public boolean send(Thriftable thriftable) {
        return putQueue(thriftable);
    }

    private boolean putQueue(Object data) {
        if (data == null) {
            logger.warning("putQueue(). data is null");
            return false;
        }
        if (!started.get()) {
            return false;
        }
        boolean offer = queue.offer(data);
        if (!offer) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Drop data. queue is full. size:" + queue.size());
            }
        }
        return offer;
    }


    @Override
    public void stop() {
        if (!started.get()) {
            return;
        }
        started.set(false);
        // io thread 안전 종료. queue 비우기.
        // TODO 종료 처리가 안이쁨. 고쳐야 될듯.
    }

    public void run() {
        doSend();
    }

    private void doSend() {
        drain:
        while (true) {
            try {
                List<Object> dtoList = takeN();
                if (dtoList != null) {
                    sendPacketN(dtoList);
                    continue;
                }

                while(true) {
                    Object dto = takeOne();
                    if (dto != null) {
                        sendPacket(dto);
                        continue drain;
                    }
                }

            } catch (Throwable th) {
                logger.log(Level.WARNING, "Unexpected Error Cause:" + th.getMessage(), th);
            }
        }
    }

    private void sendPacketN(List<Object> dtoList) {
        for (Object dto : dtoList) {
            try {
                sendPacket(dto);
            } catch (Throwable th) {
                logger.log(Level.WARNING, "Unexpected Error Cause:" + th.getMessage(), th);
            }
        }
    }

    private void sendPacket(Object dto) {
        TBase tBase;
        if (dto instanceof TBase) {
            tBase = (TBase) dto;
        } else if (dto instanceof Thriftable) {
            tBase = ((Thriftable) dto).toThrift();
        } else {
            logger.warning("sendPacket fail. invalid type:" + dto.getClass());
            return;
        }
        byte[] sendData = serialize(tBase);
        if (sendData == null) {
            logger.warning("sendData is null");
            return;
        }
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length);
        try {
            udpSocket.send(packet);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Data sent. " + dto);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "packet send error " + dto, e);
        }
    }

    private Object takeOne() {
        try {
            return queue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    private List<Object> takeN() {
        drain.clear();
        int size = queue.drainTo(drain, 10);
        if (size <= 0) {
            return null;
        }
        return drain;
    }

    private byte[] serialize(TBase<?, ?> dto) {
        try {
            Header header = headerLookup(dto);
            return serializer.serialize(header, dto);
        } catch (TException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Serialize fail:" + dto + " Caused:" + e.getMessage(), e);
            }
            return null;
        }
    }

    private Header headerLookup(TBase<?, ?> dto) throws TException {
        // header 객체 생성을 안하고 정적 lookup이 되도록 변경.
        return locator.headerLookup(dto);
    }
}
