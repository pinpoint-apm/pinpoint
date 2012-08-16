package com.profiler.receiver.udp;

import com.profiler.data.read.ReadHandler;
import com.profiler.data.read.ReadJVMData;
import com.profiler.data.read.ReadRequestData;
import com.profiler.data.read.ReadRequestTransactionData;
import com.profiler.dto.JVMInfoThriftDTO;
import com.profiler.dto.RequestDataDTO;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestThriftDTO;
import com.profiler.util.DefaultTBaseLocator;
import com.profiler.util.HeaderTBaseDeserializer;
import com.profiler.util.TBaseLocator;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.net.DatagramPacket;

public class MulplexedPacketHandler implements Runnable {
    private DatagramPacket datagramPacket;

    private TBaseLocator locator= new DefaultTBaseLocator();

    public MulplexedPacketHandler(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;

    }

    @Override
    public void run() {
        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        TBase<?,?> tBase = null;
        try {
            tBase = deserializer.deserialize(locator, datagramPacket.getData());
            dispatch(tBase, datagramPacket);
        } catch (TException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private ReadJVMData readJVMData = new ReadJVMData();
    private ReadRequestData readRequestData = new ReadRequestData();
    private ReadRequestTransactionData readRequestTransactionData = new ReadRequestTransactionData();

    private void dispatch(TBase<?, ?> tBase, DatagramPacket datagramPacket) {
        ReadHandler readHandler = getReadHandler(tBase, datagramPacket);
        readHandler.handler(tBase, datagramPacket);
    }

    private ReadHandler getReadHandler(TBase<?, ?> tBase, DatagramPacket datagramPacket) {
        if(tBase instanceof JVMInfoThriftDTO) {
            return readJVMData;
        }
        if(tBase instanceof RequestDataListThriftDTO) {
            return readRequestData;
        }
        if(tBase instanceof RequestThriftDTO) {
            return readRequestTransactionData;
        }
        throw new UnsupportedOperationException();
    }
}
