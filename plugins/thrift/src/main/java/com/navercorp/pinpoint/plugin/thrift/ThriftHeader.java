/*
\ * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.EnumMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TType;

/**
 * @author HyunGil Jeong
 */
public class ThriftHeader {

    private EnumMap<ThriftHeaderKey, Object> thriftHeaders = new EnumMap<ThriftHeaderKey, Object>(ThriftHeaderKey.class);
    
    // TRACE_ID
    
    public String getTraceId() {
        return this.getTraceId(null);
    }
    
    public String getTraceId(String defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.TRACE_ID)) {
            return (String)this.thriftHeaders.get(ThriftHeaderKey.TRACE_ID);
        }
        return defaultValue;
    }
    
    public void setTraceId(String traceId) {
        this.thriftHeaders.put(ThriftHeaderKey.TRACE_ID, traceId);
    }
    
    // SPAN_ID
    
    public Long getSpanId() {
        return this.getSpanId(null);
    }
    
    public Long getSpanId(Long defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.SPAN_ID)) {
            return (Long)this.thriftHeaders.get(ThriftHeaderKey.SPAN_ID);
        }
        return defaultValue;
    }
    
    public void setSpanId(Long spanId) {
        this.thriftHeaders.put(ThriftHeaderKey.SPAN_ID, spanId);
    }
    
    // PARENT_SPAN_ID
    
    public Long getParentSpanId() {
        return this.getParentSpanId(null);
    }
    
    public Long getParentSpanId(Long defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.PARENT_SPAN_ID)) {
            return (Long)this.thriftHeaders.get(ThriftHeaderKey.PARENT_SPAN_ID);
        }
        return defaultValue;
    }
    
    public void setParentSpanId(Long parentSpanId) {
        this.thriftHeaders.put(ThriftHeaderKey.PARENT_SPAN_ID, parentSpanId);
    }

    // SAMPLED
    
    public Boolean shouldSample() {
        return this.shouldSample(null);
    }
    
    public Boolean shouldSample(Boolean defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.SAMPLED)) {
            return (Boolean)this.thriftHeaders.get(ThriftHeaderKey.SAMPLED);
        }
        return defaultValue;
    }
    
    public void setShouldSample(Boolean shouldSample) {
        this.thriftHeaders.put(ThriftHeaderKey.SAMPLED, shouldSample);
    }
    
    // FLAGS
    
    public Short getFlags() {
        return this.getFlags(null);
    }
    
    public Short getFlags(Short defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.FLAGS)) {
            return (Short)this.thriftHeaders.get(ThriftHeaderKey.FLAGS);
        }
        return defaultValue;
    }
    
    public void setFlags(Short flags) {
        this.thriftHeaders.put(ThriftHeaderKey.FLAGS, flags);
    }
    
    // PARENT_APPLICATION_NAME
    
    public String getParentApplicationName() {
        return this.getParentApplicationName(null);
    }
    
    public String getParentApplicationName(String defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.PARENT_APPLICATION_NAME)) {
            return (String)this.thriftHeaders.get(ThriftHeaderKey.PARENT_APPLICATION_NAME);
        }
        return defaultValue;
    }
    
    public void setParentApplicationName(String parentApplicationName) {
        this.thriftHeaders.put(ThriftHeaderKey.PARENT_APPLICATION_NAME, parentApplicationName);
    }
    
    // PARENT_APPLICATION_TYPE
    
    public Short getParentApplicationType() {
        return this.getParentApplicationType(null);
    }
    
    public Short getParentApplicationType(Short defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.PARENT_APPLICATION_TYPE)) {
            return (Short)this.thriftHeaders.get(ThriftHeaderKey.PARENT_APPLICATION_TYPE);
        }
        return defaultValue;
    }
    
    public void setParentApplicationType(Short parentApplicationType) {
        this.thriftHeaders.put(ThriftHeaderKey.PARENT_APPLICATION_TYPE, parentApplicationType);
    }
    
    // ACCEPTOR_HOST
    
    public String getAcceptorHost() {
        return getAcceptorHost(ThriftConstants.UNKNOWN_ADDRESS);
    }
    
    public String getAcceptorHost(String acceptorHost) {
        if (this.thriftHeaders.containsKey(ThriftHeaderKey.ACCEPTOR_HOST)) {
            return (String)this.thriftHeaders.get(ThriftHeaderKey.ACCEPTOR_HOST);
        }
        return acceptorHost;
    }
    
    public void setAcceptorHost(String acceptorHost) {
        this.thriftHeaders.put(ThriftHeaderKey.ACCEPTOR_HOST, acceptorHost);
    }
    
    public void setTraceHeader(ThriftHeaderKey headerKey, Object value) throws TException {
        byte headerType = headerKey.getType();
        if (headerType == TType.STRING) {
            // skipped Strings are read as byte buffer.
            // see org.apache.thrift.protocol.TProtocolUtil.skip(TProtocol, byte, int)
            this.thriftHeaders.put(headerKey, byteBufferToString((ByteBuffer)value));
        } else if (headerType == TType.I64) {
            this.thriftHeaders.put(headerKey, (Long)value);
        } else if (headerType == TType.I16) {
            this.thriftHeaders.put(headerKey, (Short)value);
        } else if (headerType == TType.BOOL) {
            this.thriftHeaders.put(headerKey, (Boolean)value);
        } else {
            throw new TProtocolException("Invalid pinpoint header type - " + headerType);
        }
    }
    
    public void writeTraceHeader(ThriftHeaderKey headerKey, TProtocol oprot) throws TException {
        Object headerValue = this.thriftHeaders.get(headerKey);
        if (headerValue == null) {
            return;
        }
        byte headerType = headerKey.getType();
        TField traceField = new TField(headerKey.name(), headerKey.getType(), headerKey.getId());
        oprot.writeFieldBegin(traceField);
        try {
            if (headerType == TType.STRING) {
                // these will be read as byte buffer although it's probably safe to just use writeString here.
                // see org.apache.thrift.protocol.TProtocolUtil.skip(TProtocol, byte, int)
                oprot.writeBinary(stringToByteBuffer((String)headerValue));
            } else if (headerType == TType.I64) {
                oprot.writeI64((Long)headerValue);
            } else if (headerType == TType.I16) {
                oprot.writeI16((Short)headerValue);
            } else if (headerType == TType.BOOL) {
                oprot.writeBool((Boolean)headerValue);
            } else {
                throw new TProtocolException("Invalid pinpoint header type - " + headerType);
            }
        } finally {
            oprot.writeFieldEnd();
        }
    }
    
    private static final Charset HEADER_CHARSET_ENCODING = Charset.forName("UTF-8");
    
    private static ByteBuffer stringToByteBuffer(String s) {
        return ByteBuffer.wrap(s.getBytes(HEADER_CHARSET_ENCODING));
    }
    
    private static String byteBufferToString(ByteBuffer buf) {
        CharBuffer charBuffer = HEADER_CHARSET_ENCODING.decode(buf);
        return charBuffer.toString();
    }
    
    @Override
    public String toString() {
        return this.thriftHeaders.toString();
    }
    
    public static enum ThriftHeaderKey {
        TRACE_ID                (TType.STRING,  (short) Short.MIN_VALUE),
        SPAN_ID                 (TType.I64,     (short)(Short.MIN_VALUE+1)),
        PARENT_SPAN_ID          (TType.I64,     (short)(Short.MIN_VALUE+2)),
        SAMPLED                 (TType.BOOL,    (short)(Short.MIN_VALUE+3)),
        FLAGS                   (TType.I16,     (short)(Short.MIN_VALUE+4)),
        PARENT_APPLICATION_NAME (TType.STRING,  (short)(Short.MIN_VALUE+5)),
        PARENT_APPLICATION_TYPE (TType.I16,     (short)(Short.MIN_VALUE+6)),
        ACCEPTOR_HOST           (TType.STRING,  (short)(Short.MIN_VALUE+7));
    
        private final short id;
        
        private final byte type;
        
        private ThriftHeaderKey(byte type, short id) {
            this.type = type;
            this.id = (short)id;
        }
        
        public short getId() {
            return this.id;
        }
        
        public byte getType() {
            return this.type;
        }
        
        /**
         * Returns the {@link ThriftHeader} with the specified id,
         * or {@code null} if there is none.
         *
         * @param id the id of the associated <tt>ThriftHeaderKey</tt>
         * @return the <tt>ThriftHeaderKey</tt> associated with the specified id, or
         *     <tt>null</tt> if there is none
         */
        public static ThriftHeaderKey findThriftHeaderKeyById(short id) {
            for (ThriftHeaderKey headerKey : ThriftHeaderKey.values()) {
                if (headerKey.id == id) {
                    return headerKey;
                }
            }
            return null;
        }
        
    }
    
}