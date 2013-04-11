package com.profiler.common.io;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.profiler.common.util.BytesUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;

import com.profiler.common.dto2.Header;

/**
 * Generic utility for easily serializing objects into a byte array or Java
 * String.
 */
public class HeaderTBaseSerializer {

    /**
     * This is the byte array that data is actually serialized into
     */
    // udp 패킷 사이즈에 최대 맞춤.
    private final ByteArrayOutputStream baos_ = new ByteArrayOutputStream(1024 * 64);

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

//		this(new TBinaryProtocol.Factory());
        this(new TCompactProtocol.Factory());
    }

    /**
     * Create a new TSerializer. It will use the TProtocol specified by the
     * factory that is passed in.
     *
     * @param protocolFactory Factory to create a protocol
     */
    public HeaderTBaseSerializer(TProtocolFactory protocolFactory) {
        protocol_ = protocolFactory.getProtocol(transport_);
    }

    /**
     * Serialize the Thrift object into a byte array. The process is simple,
     * just clear the byte array output, write the object into it, and grab the
     * raw bytes.
     *
     * @param base The object to serialize
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
        // 프로토콜 변경에 관계 없이 고정 사이즈의 데이터로 인코딩 하도록 변경.
        short type = header.getType();
        protocol_.writeByte(BytesUtils.writeShort1(type));
        protocol_.writeByte(BytesUtils.writeShort2(type));
    }

    /**
     * Serialize the Thrift object into a Java string, using a specified
     * character set for encoding.
     *
     * @param base    The object to serialize
     * @param charset Valid JVM charset
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
     * @param base The object to serialize
     * @return Serialized object as a String
     */
    public String toString(Header header, TBase<?, ?> base) throws TException {
        return new String(serialize(header, base));
    }
}
