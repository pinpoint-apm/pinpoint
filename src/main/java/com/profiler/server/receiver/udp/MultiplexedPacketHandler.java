package com.profiler.server.receiver.udp;

import java.net.DatagramPacket;

import com.profiler.common.util.DefaultTBaseLocator;
import com.profiler.server.spring.SpringConstants;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.util.HeaderTBaseDeserializer;
import com.profiler.common.util.TBaseLocator;
import com.profiler.server.data.handler.Handler;

import org.springframework.context.support.GenericApplicationContext;

public class MultiplexedPacketHandler implements Runnable {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final DatagramPacket datagramPacket;
	private final GenericApplicationContext context;

	private static final TBaseLocator locator = new DefaultTBaseLocator();

	public MultiplexedPacketHandler(DatagramPacket datagramPacket, GenericApplicationContext context) {
		this.datagramPacket = datagramPacket;
		this.context = context;
	}

	@Override
	public void run() {
		// 캐쉬하는게 좋을거 같음.
		HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
		try {
			TBase<?, ?> tBase = deserializer.deserialize(locator, datagramPacket.getData());
			dispatch(tBase, datagramPacket);
		} catch (TException e) {
			logger.warn("packet serialize error " + e.getMessage(), e);
		}
	}

	private void dispatch(TBase<?, ?> tBase, DatagramPacket datagramPacket) {
		Handler readHandler = getReadHandler(tBase);

		if (logger.isDebugEnabled()) {
			logger.debug("handler name:" + readHandler.getClass().getName());
		}

		readHandler.handler(tBase, datagramPacket);
	}

	private Handler getReadHandler(TBase<?, ?> tBase) {
		if (tBase instanceof JVMInfoThriftDTO) {
			return context.getBean(SpringConstants.JVM_DATA_READER_BEAN_NAME, Handler.class);
		}
		if (tBase instanceof Span) {
			return context.getBean(SpringConstants.SPAN_READER_BEAN_NAME, Handler.class);
		}
		logger.warn("Unknown type of data received. data=" + tBase);

		throw new UnsupportedOperationException();
	}
}
