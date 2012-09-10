package com.profiler.receiver.udp;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.profiler.data.read.ReadHandler;
import com.profiler.data.read.ReadJVMData;
import com.profiler.data.read.ReadRequestData;
import com.profiler.data.read.ReadRequestTransactionData;
import com.profiler.dto.JVMInfoThriftDTO;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestThriftDTO;
import com.profiler.util.DefaultTBaseLocator;
import com.profiler.util.HeaderTBaseDeserializer;
import com.profiler.util.TBaseLocator;

public class MulplexedPacketHandler implements Runnable {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private DatagramPacket datagramPacket;

	private TBaseLocator locator = new DefaultTBaseLocator();

	public MulplexedPacketHandler(DatagramPacket datagramPacket) {
		this.datagramPacket = datagramPacket;

	}

	@Override
	public void run() {
		HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
		TBase<?, ?> tBase = null;
		try {
			tBase = deserializer.deserialize(locator, datagramPacket.getData());
			dispatch(tBase, datagramPacket);
		} catch (TException e) {
			logger.warn("packet serialize error " + e.getMessage(), e);
		}
	}

	private ReadJVMData readJVMData = new ReadJVMData();
	private ReadRequestData readRequestData = new ReadRequestData();
	private ReadRequestTransactionData readRequestTransactionData = new ReadRequestTransactionData();

	private void dispatch(TBase<?, ?> tBase, DatagramPacket datagramPacket) {
		ReadHandler readHandler = getReadHandler(tBase, datagramPacket);
		if (logger.isDebugEnabled()) {
			logger.debug("handler name:" + readHandler.getClass().getName());
		}
		readHandler.handler(tBase, datagramPacket);
	}

	private ReadHandler getReadHandler(TBase<?, ?> tBase, DatagramPacket datagramPacket) {
		if (tBase instanceof JVMInfoThriftDTO) {
			return readJVMData;
		}
		if (tBase instanceof RequestDataListThriftDTO) {
			return readRequestData;
		}
		if (tBase instanceof RequestThriftDTO) {
			return readRequestTransactionData;
		}
		throw new UnsupportedOperationException();
	}
}
