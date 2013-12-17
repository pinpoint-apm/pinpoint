package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TMemoryInputTransport;

/**
 * copy->TBaseDeserializer
 */
public class HeaderTBaseDeserializer {

    private final TProtocol protocol_;
    private final TMemoryInputTransport trans_;

    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();
    private final TBaseLocator locator = DEFAULT_TBASE_LOCATOR;

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
            final int validate = validate(header);
            if (validate == HeaderUtils.OK) {
                TBase<?, ?> base = locator.tBaseLookup(header.getType());
                base.read(protocol_);
                return base;
            }
            if (validate == HeaderUtils.PASS_L4) {
                return new L4Packet(header);
            }
            throw new IllegalStateException("invalid validate " + validate);
        } finally {
            trans_.clear();
            protocol_.reset();
        }
    }

    private int validate(Header header) throws TException {
        final byte signature = header.getSignature();
        final int result = HeaderUtils.validateSignature(signature);
        if (result == HeaderUtils.FAIL) {
            throw new TException("Invalid Signature:" + header);
        }
        return result;
    }

    private Header readHeader() throws TException {
        final byte signature = protocol_.readByte();
        final byte version = protocol_.readByte();
        // 프로토콜 변경에 관계 없이 고정 사이즈의 데이터로 인코딩 하도록 변경.
        final byte type1 = protocol_.readByte();
        final byte type2 = protocol_.readByte();
        final short type = bytesToShort(type1, type2);
        return new Header(signature, version, type);
    }

    private short bytesToShort(final byte byte1, final byte byte2) {
        return (short) (((byte1 & 0xff) << 8) | ((byte2 & 0xff)));
    }

}
