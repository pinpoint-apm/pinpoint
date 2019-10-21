/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.thrift.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;

/**
 * Replace list field protocol.
 * 
 * @author jaehong.kim
 */
public class TReplaceListProtocol extends TProtocol {

    private boolean writeFieldBegin = false;
    private int writeListDepth = 0;
    private Map<String, List<ByteArrayOutput>> replaceFields = new HashMap<String, List<ByteArrayOutput>>();
    private TField currentField = null;
    private TProtocol protocol;

    public TReplaceListProtocol(final TProtocol protocol) {
        super(protocol.getTransport());
        this.protocol = protocol;
    }

    public void addReplaceField(final String fieldName, List<ByteArrayOutput> outputs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("field name");
        }

        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("stream nodes must not be empty");
        }

        replaceFields.put(fieldName, outputs);
    }

    @Override
    public void writeFieldBegin(TField field) throws TException {
        if (!writeFieldBegin) {
            protocol.writeFieldBegin(field);
            if (replaceFields.containsKey(field.name)) {
                writeFieldBegin = true;
                currentField = field;
            }
        }
    }

    @Override
    public void writeFieldEnd() throws TException {
        if (!writeFieldBegin) {
            protocol.writeFieldEnd();
        } else if (writeListDepth == 0) {
            writeFieldBegin = false;
            currentField = null;
        }
    }

    @Override
    public void writeListBegin(TList list) throws TException {
        if (!writeFieldBegin) {
            protocol.writeListBegin(list);
            return;
        }

        if (writeListDepth == 0 && currentField != null) {
            List<ByteArrayOutput> outputs = replaceFields.get(currentField.name);
            if (outputs == null) {
                throw new TException("not found replace field - " + currentField.name);
            }

            final TList replaceList = new TList(list.elemType, outputs.size());
            protocol.writeListBegin(replaceList);
            for (ByteArrayOutput output : outputs) {
                try {
                    final OutputStream out = ((ByteArrayOutputStreamTransport) getTransport()).getByteArrayOutputStream();
                    output.writeTo(out);
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
            protocol.writeListEnd();
        } else {
            writeListDepth--;
        }
    }

    @Override
    public void reset() {
        protocol.reset();
    }

    @Override
    public void writeMessageBegin(TMessage message) throws TException {
        if (!writeFieldBegin) {
            protocol.writeMessageBegin(message);
        }
    }

    @Override
    public void writeMessageEnd() throws TException {
        if (!writeFieldBegin) {
            protocol.writeMessageEnd();
        }
    }

    @Override
    public void writeStructBegin(TStruct struct) throws TException {
        if (!writeFieldBegin) {
            protocol.writeStructBegin(struct);
        }
    }

    @Override
    public void writeStructEnd() throws TException {
        if (!writeFieldBegin) {
            protocol.writeStructEnd();
        }
    }

    @Override
    public void writeFieldStop() throws TException {
        if (!writeFieldBegin) {
            protocol.writeFieldStop();
        }
    }

    @Override
    public void writeMapBegin(TMap map) throws TException {
        if (!writeFieldBegin) {
            protocol.writeMapBegin(map);
        }
    }

    @Override
    public void writeMapEnd() throws TException {
        if (!writeFieldBegin) {
            protocol.writeMapEnd();
        }
    }

    @Override
    public void writeSetBegin(TSet set) throws TException {
        if (!writeFieldBegin) {
            protocol.writeSetBegin(set);
        }
    }

    @Override
    public void writeSetEnd() throws TException {
        if (!writeFieldBegin) {
            protocol.writeSetEnd();
        }
    }

    @Override
    public void writeBool(boolean b) throws TException {
        if (!writeFieldBegin) {
            protocol.writeBool(b);
        }
    }

    @Override
    public void writeByte(byte b) throws TException {
        if (!writeFieldBegin) {
            protocol.writeByte(b);
        }
    }

    @Override
    public void writeI16(short i16) throws TException {
        if (!writeFieldBegin) {
            protocol.writeI16(i16);
        }
    }

    @Override
    public void writeI32(int i32) throws TException {
        if (!writeFieldBegin) {
            protocol.writeI32(i32);
        }
    }

    @Override
    public void writeI64(long i64) throws TException {
        if (!writeFieldBegin) {
            protocol.writeI64(i64);
        }
    }

    @Override
    public void writeDouble(double dub) throws TException {
        if (!writeFieldBegin) {
            protocol.writeDouble(dub);
        }
    }

    @Override
    public void writeString(String str) throws TException {
        if (!writeFieldBegin) {
            protocol.writeString(str);
        }
    }

    @Override
    public void writeBinary(ByteBuffer bin) throws TException {
        if (!writeFieldBegin) {
            protocol.writeBinary(bin);
        }
    }

    @Override
    public ByteBuffer readBinary() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public boolean readBool() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public byte readByte() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public double readDouble() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public TField readFieldBegin() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public void readFieldEnd() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public short readI16() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public int readI32() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public long readI64() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public TList readListBegin() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public void readListEnd() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public TMap readMapBegin() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public void readMapEnd() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public TMessage readMessageBegin() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public void readMessageEnd() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public TSet readSetBegin() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public void readSetEnd() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public String readString() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public TStruct readStructBegin() throws TException {
        throw new TException("unsupported operation");
    }

    @Override
    public void readStructEnd() throws TException {
        throw new TException("unsupported operation");
    }
}