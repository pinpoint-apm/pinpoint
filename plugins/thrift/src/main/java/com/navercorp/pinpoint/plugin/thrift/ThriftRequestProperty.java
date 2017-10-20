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

import com.navercorp.pinpoint.common.Charsets;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TType;

/**
 * @author HyunGil Jeong
 */
public class ThriftRequestProperty {

    private EnumMap<ThriftHeader, Object> thriftHeaders = new EnumMap<ThriftHeader, Object>(ThriftHeader.class);
    
    // TRACE_ID
    
    public String getTraceId() {
        return this.getTraceId(null);
    }
    
    public String getTraceId(String defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRIFT_TRACE_ID)) {
            return (String)this.thriftHeaders.get(ThriftHeader.THRIFT_TRACE_ID);
        }
        return defaultValue;
    }
    
    public void setTraceId(String traceId) {
        this.thriftHeaders.put(ThriftHeader.THRIFT_TRACE_ID, traceId);
    }
    
    // SPAN_ID
    
    public Long getSpanId() {
        return this.getSpanId(null);
    }
    
    public Long getSpanId(Long defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRIFT_SPAN_ID)) {
            return (Long)this.thriftHeaders.get(ThriftHeader.THRIFT_SPAN_ID);
        }
        return defaultValue;
    }
    
    public void setSpanId(Long spanId) {
        this.thriftHeaders.put(ThriftHeader.THRIFT_SPAN_ID, spanId);
    }
    
    // PARENT_SPAN_ID
    
    public Long getParentSpanId() {
        return this.getParentSpanId(null);
    }
    
    public Long getParentSpanId(Long defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRIFT_PARENT_SPAN_ID)) {
            return (Long)this.thriftHeaders.get(ThriftHeader.THRIFT_PARENT_SPAN_ID);
        }
        return defaultValue;
    }
    
    public void setParentSpanId(Long parentSpanId) {
        this.thriftHeaders.put(ThriftHeader.THRIFT_PARENT_SPAN_ID, parentSpanId);
    }

    // SAMPLED
    
    public Boolean shouldSample() {
        return this.shouldSample(null);
    }
    
    public Boolean shouldSample(Boolean defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRFIT_SAMPLED)) {
            return (Boolean)this.thriftHeaders.get(ThriftHeader.THRFIT_SAMPLED);
        }
        return defaultValue;
    }
    
    public void setShouldSample(Boolean shouldSample) {
        this.thriftHeaders.put(ThriftHeader.THRFIT_SAMPLED, shouldSample);
    }
    
    // FLAGS
    
    public Short getFlags() {
        return this.getFlags(null);
    }
    
    public Short getFlags(Short defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRIFT_FLAGS)) {
            return (Short)this.thriftHeaders.get(ThriftHeader.THRIFT_FLAGS);
        }
        return defaultValue;
    }
    
    public void setFlags(Short flags) {
        this.thriftHeaders.put(ThriftHeader.THRIFT_FLAGS, flags);
    }
    
    // PARENT_APPLICATION_NAME
    
    public String getParentApplicationName() {
        return this.getParentApplicationName(null);
    }
    
    public String getParentApplicationName(String defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRIFT_PARENT_APPLICATION_NAME)) {
            return (String)this.thriftHeaders.get(ThriftHeader.THRIFT_PARENT_APPLICATION_NAME);
        }
        return defaultValue;
    }
    
    public void setParentApplicationName(String parentApplicationName) {
        this.thriftHeaders.put(ThriftHeader.THRIFT_PARENT_APPLICATION_NAME, parentApplicationName);
    }
    
    // PARENT_APPLICATION_TYPE
    
    public Short getParentApplicationType() {
        return this.getParentApplicationType(null);
    }
    
    public Short getParentApplicationType(Short defaultValue) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRIFT_PARENT_APPLICATION_TYPE)) {
            return (Short)this.thriftHeaders.get(ThriftHeader.THRIFT_PARENT_APPLICATION_TYPE);
        }
        return defaultValue;
    }
    
    public void setParentApplicationType(Short parentApplicationType) {
        this.thriftHeaders.put(ThriftHeader.THRIFT_PARENT_APPLICATION_TYPE, parentApplicationType);
    }
    
    // ACCEPTOR_HOST
    
    public String getAcceptorHost() {
        return getAcceptorHost(ThriftConstants.UNKNOWN_ADDRESS);
    }
    
    public String getAcceptorHost(String acceptorHost) {
        if (this.thriftHeaders.containsKey(ThriftHeader.THRIFT_HOST)) {
            return (String)this.thriftHeaders.get(ThriftHeader.THRIFT_HOST);
        }
        return acceptorHost;
    }
    
    public void setAcceptorHost(String acceptorHost) {
        this.thriftHeaders.put(ThriftHeader.THRIFT_HOST, acceptorHost);
    }
    
    public void setTraceHeader(ThriftHeader headerKey, Object value) throws TException {
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
    
    public void writeTraceHeader(ThriftHeader headerKey, TProtocol oprot) throws TException {
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
    
    private static final Charset HEADER_CHARSET_ENCODING = Charsets.UTF_8;
    
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
    
}