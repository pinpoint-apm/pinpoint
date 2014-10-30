package com.nhn.pinpoint.thrift.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;

import com.nhn.pinpoint.thrift.io.UnsafeByteArrayOutputStream;

public class TBaseStream {

    protected final LinkedList<TBaseStreamNode> nodes = new LinkedList<TBaseStreamNode>();
    protected final UnsafeByteArrayOutputStream out;
    
    public TBaseStream(final int size) {
        out = new UnsafeByteArrayOutputStream(size);
    }

    public void write(final List<TBase<?, ?>> list) throws TException {
        for (TBase<?, ?> base : list) {
            write(base);
        }
    }

    public void write(final TBase<?, ?> base) throws TException {
        final TBaseStreamNode node = new TBaseStreamNode(out);
        node.setClassName(base.getClass().getName());
        node.setBeginPosition(out.size());

        final TProtocol protocol = new TCompactProtocol(new ByteArrayOutputStreamTransport(out));
        base.write(protocol);

        node.setEndPosition(out.size());
        nodes.add(node);
    }


    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public int size() {
        int size = 0;
        for (TBaseStreamNode node : nodes) {
            size += node.size();
        }

        return size;
    }

    public List<TBaseStreamNode> splitAll() {
        final List<TBaseStreamNode> list = new ArrayList<TBaseStreamNode>();
        TBaseStreamNode node = null;
        while ((node = nodes.peek()) != null) {
            list.add(node);
            nodes.poll();
        }

        return list;
    }

    public List<TBaseStreamNode> split(final int maxSize) {
        final List<TBaseStreamNode> list = new ArrayList<TBaseStreamNode>();
        int currentSize = 0;
        TBaseStreamNode node = null;
        while ((node = nodes.peek()) != null) {
            if (node.size() > maxSize) {
                if (list.size() == 0) {
                    // first node
                    list.add(node);
                    nodes.poll();
                }
                break;
            }

            if (currentSize + node.size() > maxSize) {
                break;
            }

            currentSize += node.size();
            list.add(node);
            nodes.poll();
        }

        return list;
    }

    public void clear() {
        nodes.clear();
        out.reset();
    }
    
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("nodes=").append(nodes).append(", ");
        sb.append("size=").append(out.size());
        sb.append("}");

        return nodes.toString();
    }
}