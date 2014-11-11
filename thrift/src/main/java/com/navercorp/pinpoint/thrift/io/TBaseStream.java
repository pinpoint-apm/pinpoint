package com.nhn.pinpoint.thrift.io;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import com.nhn.pinpoint.thrift.io.UnsafeByteArrayOutputStream;

/**
 * TBase stream(byte arrays)
 * 
 * @author jaehong.kim
 */
public class TBaseStream {
    private final TProtocolFactory protocolFactory;
    private final ByteArrayOutputStreamTransport transport;
    private final LinkedList<TBaseStreamNode> nodes = new LinkedList<TBaseStreamNode>();

    public TBaseStream(final TProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
        this.transport = new ByteArrayOutputStreamTransport(new UnsafeByteArrayOutputStream());
    }

    public void write(final List<TBase<?, ?>> list) throws TException {
        for (TBase<?, ?> base : list) {
            write(base);
        }
    }

    public void write(final TBase<?, ?> base) throws TException {
        final TBaseStreamNode node = new TBaseStreamNode(transport);
        node.setClassName(base.getClass().getName());
        node.setBeginPosition(transport.getBufferPosition());

        final TProtocol protocol = protocolFactory.getProtocol(transport);
        base.write(protocol);

        node.setEndPosition(transport.getBufferPosition());
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

    public List<ByteArrayOutput> splitAll() {
        final List<ByteArrayOutput> list = new ArrayList<ByteArrayOutput>();
        TBaseStreamNode node = null;
        while ((node = nodes.peek()) != null) {
            list.add(node);
            nodes.poll();
        }

        return list;
    }

    public List<ByteArrayOutput> split(final int maxSize) {
        final List<ByteArrayOutput> list = new ArrayList<ByteArrayOutput>();
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

    public void clear() throws TException {
        nodes.clear();
        transport.flush();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("nodes=").append(nodes).append(", ");
        sb.append("transport=").append(transport);
        sb.append("}");

        return nodes.toString();
    }
}