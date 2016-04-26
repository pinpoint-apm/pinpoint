package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.io.OutputStream;

/**
 *
 * Caution. not thread safe
 *
 * @Author Taejin Koo
 */
public class HeaderTBaseSerializer2 {

    private static final String UTF8 = "UTF8";

    private final TOutputStreamTransport tOutputStreamTransport;
    private final TProtocol protocol;
    private final TBaseLocator tBaseLocator;

    public HeaderTBaseSerializer2(TProtocolFactory protocolFactory, TBaseLocator tBaseLocator) {
        this.tOutputStreamTransport = new TOutputStreamTransport();
        this.protocol = protocolFactory.getProtocol(tOutputStreamTransport);
        this.tBaseLocator = tBaseLocator;
    }

    public void serialize(TBase<?, ?> base, OutputStream outputStream) throws TException {
        tOutputStreamTransport.open(outputStream);
        try {
            final Header header = tBaseLocator.headerLookup(base);
            writeHeader(header);
            base.write(protocol);
        } finally {
            tOutputStreamTransport.close();
        }
    }

    private void writeHeader(Header header) throws TException {
        protocol.writeByte(header.getSignature());
        protocol.writeByte(header.getVersion());
        // fixed size regardless protocol
        short type = header.getType();
        protocol.writeByte(BytesUtils.writeShort1(type));
        protocol.writeByte(BytesUtils.writeShort2(type));
    }

}
