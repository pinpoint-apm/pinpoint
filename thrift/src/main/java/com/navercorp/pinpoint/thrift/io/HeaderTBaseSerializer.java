package com.nhn.pinpoint.thrift.io;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;

/**
 * Generic utility for easily serializing objects into a byte array or Java
 * String.
 * copy->HeaderTBaseSerializer
 */
public class HeaderTBaseSerializer {

    private final ByteArrayOutputStream baos;
    private final TProtocol protocol;
    private final TBaseLocator locator;

    /**
     * Create a new HeaderTBaseSerializer. 
     */
    HeaderTBaseSerializer(ByteArrayOutputStream bos, TProtocolFactory protocolFactory, TBaseLocator locator) {
    	this.baos = bos;
        TIOStreamTransport transport = new TIOStreamTransport(bos);
        this.protocol = protocolFactory.getProtocol(transport);
        this.locator = locator;
    }
	
    /**
     * Serialize the Thrift object into a byte array. The process is simple,
     * just clear the byte array output, write the object into it, and grab the
     * raw bytes.
     *
     * @param base The object to serialize
     * @return Serialized object in byte[] format
     */
    public byte[] serialize(TBase<?, ?> base) throws TException {
        final Header header = locator.headerLookup(base);
        baos.reset();
        writeHeader(header);
        base.write(protocol);
        return baos.toByteArray();
    }

    public int getInterBufferSize() {
        return baos.size();
    }

    private void writeHeader(Header header) throws TException {
    	protocol.writeByte(header.getSignature());
    	protocol.writeByte(header.getVersion());
        // 프로토콜 변경에 관계 없이 고정 사이즈의 데이터로 인코딩 하도록 변경.
        short type = header.getType();
        protocol.writeByte(BytesUtils.writeShort1(type));
        protocol.writeByte(BytesUtils.writeShort2(type));
    }

    /**
     * Serialize the Thrift object into a Java string, using the default JVM
     * charset encoding.
     *
     * @param base The object to serialize
     * @return Serialized object as a String
     */
    public String toString(TBase<?, ?> base) throws TException {
        return new String(serialize(base));
    }
 
    /**
     * Serialize the Thrift object into a Java string, using a specified
     * character set for encoding.
     *
     * @param base    The object to serialize
     * @param charset Valid JVM charset
     * @return Serialized object as a String
     */
    public String toString(TBase<?, ?> base, String charset) throws TException {
        try {
            return new String(serialize(base), charset);
        } catch (UnsupportedEncodingException uex) {
            throw new TException("JVM DOES NOT SUPPORT ENCODING: " + charset);
        }
    }

   
}
