package com.nhn.pinpoint.thrift.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;

import com.nhn.pinpoint.thrift.io.UnsafeByteArrayOutputStream;

public class TBaseStreamNode {

    private final UnsafeByteArrayOutputStream stream;
    private int beginPosition;
    private int endPosition;
    private String className;

    public TBaseStreamNode(final UnsafeByteArrayOutputStream stream) {
        this.stream = stream;
    }

    public int getBeginPosition() {
        return beginPosition;
    }

    public void setBeginPosition(int beginPosition) {
        this.beginPosition = beginPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int size() {
        return endPosition - beginPosition;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(stream.toByteArray(), beginPosition, size());
    }

    public void writeTo(DatagramPacket packet) {
        packet.setData(stream.toByteArray(), beginPosition, size());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{beginPosition=");
        builder.append(beginPosition);
        builder.append(", endPosition=");
        builder.append(endPosition);
        builder.append(", className=");
        builder.append(className);
        builder.append(", size=");
        builder.append(size());
        builder.append("}");
        return builder.toString();
    }
}