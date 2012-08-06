package com.profiler.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;

import com.profiler.dto.Header;

/**
 * Generic utility for easily serializing objects into a byte array or Java
 * String.
 * 
 */
public class HeaderTBaseSerializer {

	/**
	 * This is the byte array that data is actually serialized into
	 */
	private final ByteArrayOutputStream baos_ = new ByteArrayOutputStream();

	/**
	 * This transport wraps that byte array
	 */
	private final TIOStreamTransport transport_ = new TIOStreamTransport(baos_);

	/**
	 * Internal protocol used for serializing objects.
	 */
	private TProtocol protocol_;

	/**
	 * Create a new TSerializer that uses the TBinaryProtocol by default.
	 */
	public HeaderTBaseSerializer() {
		this(new TBinaryProtocol.Factory());
	}

	/**
	 * Create a new TSerializer. It will use the TProtocol specified by the
	 * factory that is passed in.
	 * 
	 * @param protocolFactory
	 *            Factory to create a protocol
	 */
	public HeaderTBaseSerializer(TProtocolFactory protocolFactory) {
		protocol_ = protocolFactory.getProtocol(transport_);
	}

	/**
	 * Serialize the Thrift object into a byte array. The process is simple,
	 * just clear the byte array output, write the object into it, and grab the
	 * raw bytes.
	 * 
	 * @param base
	 *            The object to serialize
	 * @return Serialized object in byte[] format
	 */
	public byte[] serialize(Header header, TBase<?, ?> base) throws TException {
		baos_.reset();
		writeHeader(header);
		base.write(protocol_);
		return baos_.toByteArray();
	}

	private void writeHeader(Header header) throws TException {
		protocol_.writeByte(header.getSignature());
		protocol_.writeByte(header.getVersion());
		protocol_.writeI16(header.getType());
	}

	/**
	 * Serialize the Thrift object into a Java string, using a specified
	 * character set for encoding.
	 * 
	 * @param base
	 *            The object to serialize
	 * @param charset
	 *            Valid JVM charset
	 * @return Serialized object as a String
	 */
	public String toString(Header header, TBase<?, ?> base, String charset) throws TException {
		try {
			return new String(serialize(header, base), charset);
		} catch (UnsupportedEncodingException uex) {
			throw new TException("JVM DOES NOT SUPPORT ENCODING: " + charset);
		}
	}

	/**
	 * Serialize the Thrift object into a Java string, using the default JVM
	 * charset encoding.
	 * 
	 * @param base
	 *            The object to serialize
	 * @return Serialized object as a String
	 */
	public String toString(Header header, TBase<?, ?> base) throws TException {
		return new String(serialize(header, base));
	}
}
