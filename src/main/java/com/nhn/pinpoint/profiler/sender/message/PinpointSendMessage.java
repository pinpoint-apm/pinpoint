package com.nhn.pinpoint.profiler.sender.message;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author koo.taejin
 */
public class PinpointSendMessage implements PinpointMessage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final TBase tBase;
	private final HeaderTBaseSerializer serializer;

	public PinpointSendMessage(TBase tBase, HeaderTBaseSerializer serializer) {
		this.tBase = tBase;
		this.serializer = serializer;
	}

	@Override
	public byte[] serialize() {
		try {
			return serializer.serialize(tBase);
		} catch (TException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Serialize fail:{} Caused:{}", tBase, e.getMessage(), e);
			}
			return null;
		}
	}

	@Override
	public TBase getTBase() {
		return tBase;
	}

}
