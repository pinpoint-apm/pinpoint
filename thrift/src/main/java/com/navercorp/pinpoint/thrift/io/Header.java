package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public class Header {

    public static final byte SIGNATURE = (byte) 0xef;

    public static final int HEADER_SIZE = 4;

    private byte signature = SIGNATURE;
    private byte version = 0x10;
    private short type = 0;

    public Header() {
    }

    public Header(byte signature, byte version, short type) {
        this.signature = signature;
        this.version = version;
        this.type = type;
    }

    public byte getSignature() {
        return signature;
    }

    public void setSignature(byte signature) {
        this.signature = signature;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public byte[] getBytes() {
        final byte[] buffer = new byte[HEADER_SIZE];
        buffer[0] = signature;
        buffer[1] = version;
        buffer[2] = BytesUtils.writeShort1(type);
        buffer[3] = BytesUtils.writeShort2(type);

        return buffer;
    }
    
    
    @Override
    public String toString() {
        return "Header{" +
                "signature=" + signature +
                ", version=" + version +
                ", type=" + type +
                '}';
    }
}

