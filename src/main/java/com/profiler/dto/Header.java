package com.profiler.dto;

public class Header {

    private byte signature = (byte) 0xef;
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
}

