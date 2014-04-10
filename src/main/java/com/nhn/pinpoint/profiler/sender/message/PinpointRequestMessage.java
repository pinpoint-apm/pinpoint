package com.nhn.pinpoint.profiler.sender.message;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author koo.taejin
 */
public class PinpointRequestMessage implements PinpointMessage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final TBase tBase;
	private final int retryCount;
	
	private final HeaderTBaseSerializer serializer;

	public PinpointRequestMessage(TBase tBase, int retryCount, HeaderTBaseSerializer serializer) {
		this.tBase = tBase;
		this.retryCount = retryCount;
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

	public int getRetryCount() {
		return retryCount;
	}
}
