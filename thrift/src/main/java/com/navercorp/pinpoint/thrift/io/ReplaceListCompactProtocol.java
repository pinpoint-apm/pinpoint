package com.nhn.pinpoint.thrift.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;

public class ReplaceListCompactProtocol extends TCompactProtocol {

    private boolean writeFieldBegin = false;
    private int writeListDepth = 0;
    private Map<String, List<TBaseStreamNode>> replaceFields = new HashMap<String, List<TBaseStreamNode>>();
    private TField currentField = null;

    public ReplaceListCompactProtocol(final ByteArrayOutputStreamTransport transport) {
        super(transport);
    }

    public void addReplaceField(final String fieldName, List<TBaseStreamNode> streamNodes) {
        if (fieldName == null) {
            throw new IllegalArgumentException("field name must not be null");
        }

        if (streamNodes == null || streamNodes.size() == 0) {
            throw new IllegalArgumentException("stream nodes must not be empty");
        }

        replaceFields.put(fieldName, streamNodes);
    }

    @Override
    public void writeFieldBegin(TField field) throws TException {
        if (!writeFieldBegin) {
            super.writeFieldBegin(field);
            if (replaceFields.containsKey(field.name)) {
                writeFieldBegin = true;
                currentField = field;
            }
        }
    }

    @Override
    public void writeFieldEnd() throws TException {
        if (!writeFieldBegin) {
            super.writeFieldEnd();
        } else if (writeListDepth == 0) {
            writeFieldBegin = false;
            currentField = null;
        }
    }

    @Override
    public void writeListBegin(TList list) throws TException {
        if (!writeFieldBegin) {
            super.writeCollectionBegin(list.elemType, list.size);
            return;
        }

        if (writeListDepth == 0 && currentField != null) {
            List<TBaseStreamNode> nodes = replaceFields.get(currentField.name);
            if (nodes == null) {
                throw new TException("not found replace field - " + currentField.name);
            }

            super.writeCollectionBegin(list.elemType, nodes.size());
            for (TBaseStreamNode node : nodes) {
                try {
                    final OutputStream out = ((ByteArrayOutputStreamTransport) getTransport()).getByteArrayOutputStream();
                    node.writeTo(out);
                } catch (IOException e) {
                    throw new TException(e);
                }
            }
        }

        writeListDepth++;
    }

    @Override
    public void writeListEnd() throws TException {
        if (!writeFieldBegin) {
            super.writeListEnd();
        } else {
            writeListDepth--;
        }
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void writeMessageBegin(TMessage message) throws TException {
        if (!writeFieldBegin) {
            super.writeMessageBegin(message);
        }
    }

    @Override
    public void writeMessageEnd() throws TException {
        if (!writeFieldBegin) {
            super.writeMessageEnd();
        }
    }

    @Override
    public void writeStructBegin(TStruct struct) throws TException {
        if (!writeFieldBegin) {
            super.writeStructBegin(struct);
        }
    }

    @Override
    public void writeStructEnd() throws TException {
        if (!writeFieldBegin) {
            super.writeStructEnd();
        }
    }

    @Override
    public void writeFieldStop() throws TException {
        if (!writeFieldBegin) {
            super.writeFieldStop();
        }
    }

    @Override
    public void writeMapBegin(TMap map) throws TException {
        if (!writeFieldBegin) {
            super.writeMapBegin(map);
        }
    }

    @Override
    public void writeMapEnd() throws TException {
        if (!writeFieldBegin) {
            super.writeMapEnd();
        }
    }

    @Override
    public void writeSetBegin(TSet set) throws TException {
        if (!writeFieldBegin) {
            super.writeSetBegin(set);
        }
    }

    @Override
    public void writeSetEnd() throws TException {
        if (!writeFieldBegin) {
            super.writeSetEnd();
        }
    }

    @Override
    public void writeBool(boolean b) throws TException {
        if (!writeFieldBegin) {
            super.writeBool(b);
        }
    }

    @Override
    public void writeByte(byte b) throws TException {
        if (!writeFieldBegin) {
            super.writeByte(b);
        }
    }

    @Override
    public void writeI16(short i16) throws TException {
        if (!writeFieldBegin) {
            super.writeI16(i16);
        }
    }

    @Override
    public void writeI32(int i32) throws TException {
        if (!writeFieldBegin) {
            super.writeI32(i32);
        }
    }

    @Override
    public void writeI64(long i64) throws TException {
        if (!writeFieldBegin) {
            super.writeI64(i64);
        }
    }

    @Override
    public void writeDouble(double dub) throws TException {
        if (!writeFieldBegin) {
            super.writeDouble(dub);
        }
    }

    @Override
    public void writeString(String str) throws TException {
        if (!writeFieldBegin) {
            super.writeString(str);
        }
    }

    @Override
    public void writeBinary(ByteBuffer bin) throws TException {
        if (!writeFieldBegin) {
            super.writeBinary(bin);
        }
    }
}