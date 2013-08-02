package com.nhn.pinpoint.common.io;

import com.nhn.pinpoint.common.dto2.Header;

import com.nhn.pinpoint.common.util.BytesUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TMemoryInputTransport;

public class HeaderTBaseDeserializer {

    private final TProtocol protocol_;
    private final TMemoryInputTransport trans_;

    private final TBaseLocator locator = new DefaultTBaseLocator();
    /**
     * Create a new TDeserializer that uses the TBinaryProtocol by default.
     */
    public HeaderTBaseDeserializer() {
//        this(new TBinaryProtocol.Factory());
        this(new TCompactProtocol.Factory());
    }

    /**
     * Create a new TDeserializer. It will use the TProtocol specified by the
     * factory that is passed in.
     *
     * @param protocolFactory Factory to create a protocol
     */
    public HeaderTBaseDeserializer(TProtocolFactory protocolFactory) {
        trans_ = new TMemoryInputTransport();
        protocol_ = protocolFactory.getProtocol(trans_);
    }

    /**
     * Deserialize the Thrift object from a byte array.
     *
     * @param bytes   The array to read from
     */
    public TBase<?, ?> deserialize(byte[] bytes) throws TException {
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

    private void validate(Header header) throws TException {
        boolean accepted = HeaderUtils.validateSignature(header.getSignature());
        if (!accepted) {
            throw new TException("Invalid Signature:" + header);
        }
    }

    private Header readHeader() throws TException {
        byte signature = protocol_.readByte();
        byte version = protocol_.readByte();
        // 프로토콜 변경에 관계 없이 고정 사이즈의 데이터로 인코딩 하도록 변경.
        byte type1 = protocol_.readByte();
        byte type2 = protocol_.readByte();
        short type = BytesUtils.bytesToShort(type1, type2);
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
