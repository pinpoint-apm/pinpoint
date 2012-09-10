package com.profiler.common.util;

import com.profiler.common.dto.Header;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TMemoryInputTransport;

public class HeaderTBaseDeserializer {
	
	private final TProtocol protocol_;
	private final TMemoryInputTransport trans_;

	/**
	 * Create a new TDeserializer that uses the TBinaryProtocol by default.
	 */
	public HeaderTBaseDeserializer() {
		this(new TBinaryProtocol.Factory());
	}

	/**
	 * Create a new TDeserializer. It will use the TProtocol specified by the
	 * factory that is passed in.
	 * 
	 * @param protocolFactory
	 *            Factory to create a protocol
	 */
	public HeaderTBaseDeserializer(TProtocolFactory protocolFactory) {
		trans_ = new TMemoryInputTransport();
		protocol_ = protocolFactory.getProtocol(trans_);
	}

	/**
	 * Deserialize the Thrift object from a byte array.
	 * 
	 * @param locator
	 *            The object to read into
	 * @param bytes
	 *            The array to read from
	 */
	public TBase<?, ?> deserialize(TBaseLocator locator, byte[] bytes) throws TException {
		try {
			trans_.reset(bytes);
			Header header = readHeader();
			validate(header);
			TBase<?, ?> base = locator.tBaseLookup(header.getType());
			base.read(protocol_);
			return base;
		} finally {
			trans_.clear();
			protocol_.reset();
		}
	}

	private void validate(Header header) {
		boolean accepted = HeaderUtil.validateSignature(header.getSignature());
		if (!accepted) {
			throw new IllegalArgumentException("Invalid Signature:" + header);
		}
	}

	private Header readHeader() throws TException {
		byte signature = protocol_.readByte();
		byte version = protocol_.readByte();
		short type = protocol_.readI16();
		return new Header(signature, version, type);
	}

	/**
	 * Deserialize the Thrift object from a Java string, using a specified
	 * character set for decoding.
	 * 
	 * @param base
	 *            The object to read into
	 * @param data
	 *            The string to read from
	 * @param charset
	 *            Valid JVM charset
	 */
	// public void deserialize(TBase base, String data, String charset) throws
	// TException {
	// try {
	// deserialize(base, data.getBytes(charset));
	// } catch (UnsupportedEncodingException uex) {
	// throw new TException("JVM DOES NOT SUPPORT ENCODING: " + charset);
	// } finally {
	// protocol_.reset();
	// }
	// }

	/**
	 * Deserialize the Thrift object from a Java string, using the default JVM
	 * charset encoding.
	 * 
	 * @param base
	 *            The object to read into
	 * @param data
	 *            The string to read from
	 */
	// public void fromString(TBase base, String data) throws TException {
	// deserialize(base, data.getBytes());
	// }
}
