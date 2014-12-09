package com.navercorp.pinpoint.rpc.util;

import java.util.Map;

import com.navercorp.pinpoint.rpc.control.ControlMessageDecoder;
import com.navercorp.pinpoint.rpc.control.ControlMessageEncoder;
import com.navercorp.pinpoint.rpc.control.ProtocolException;

/**
 * @author koo.taejin
 */
public class ControlMessageEncodingUtils {

	private static final ControlMessageEncoder encoder = new ControlMessageEncoder();
	private static final ControlMessageDecoder decoder = new ControlMessageDecoder();

	private ControlMessageEncodingUtils() {
	}

	public static byte[] encode(Map<String, Object> value) throws ProtocolException {
		return encoder.encode(value);
	}

	public static Object decode(byte[] in) throws ProtocolException {
		return decoder.decode(in);
	}

}
