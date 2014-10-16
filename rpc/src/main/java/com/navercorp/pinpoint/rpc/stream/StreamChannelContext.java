package com.nhn.pinpoint.rpc.stream;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author koo.taejin <kr14910>
 */
public abstract class StreamChannelContext {

	private final ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<String, Object>();

	public StreamChannelContext() {
	}

	abstract public StreamChannel getStreamChannel();

	public int getStreamId() {
		return getStreamChannel().getStreamId();
	}

	public final Object getAttribute(String key) {
		return attribute.get(key);
	}

	public final Object setAttributeIfAbsent(String key, Object value) {
		return attribute.putIfAbsent(key, value);
	}

	public final Object removeAttribute(String key) {
		return attribute.remove(key);
	}
	
	public boolean isServer() {
		return getStreamChannel().isServer();
	}
	
	@Override
	public String toString() {
		return getStreamChannel().toString();
	}

}
