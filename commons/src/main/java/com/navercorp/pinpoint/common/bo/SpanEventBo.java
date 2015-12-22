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

package com.navercorp.pinpoint.common.bo;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.*;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanEventBo implements Span {
    private static final int VERSION_SIZE = 1;
   // version 0 means that the type of prefix's size is int

    private byte version = 0;

    private String agentId;
    private String applicationId;
    private long agentStartTime;

    private String traceAgentId;
    private long traceAgentStartTime;
    private long traceTransactionSequence;

    private long spanId;
    private short sequence;

    private int startElapsed;
    private int endElapsed;

    private String rpc;
    private short serviceType;

    private String destinationId;
    private String endPoint;
    private int apiId;

    private List<AnnotationBo> annotationBoList;

    private int depth = -1;
    private long nextSpanId = -1;

    private boolean hasException;
    private int exceptionId;
    private String exceptionMessage;

    // should get exceptionClass from dao
    private String exceptionClass;

    private int asyncId = -1;
    private int nextAsyncId = -1;
    private short asyncSequence = -1;
    
    public SpanEventBo() {
    }

    public SpanEventBo(TSpan tSpan, TSpanEvent tSpanEvent) {
        if (tSpan == null) {
            throw new NullPointerException("tSpan must not be null");
        }
        if (tSpanEvent == null) {
            throw new NullPointerException("tSpanEvent must not be null");
        }

        this.agentId = tSpan.getAgentId();
        this.applicationId = tSpan.getApplicationName();
        this.agentStartTime = tSpan.getAgentStartTime();

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(tSpan.getTransactionId());
        this.traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            traceAgentId = this.agentId;
        }
        this.traceAgentStartTime = transactionId.getAgentStartTime();
        this.traceTransactionSequence = transactionId.getTransactionSequence();

        this.spanId = tSpan.getSpanId();
        this.sequence = tSpanEvent.getSequence();

        this.startElapsed = tSpanEvent.getStartElapsed();
        this.endElapsed = tSpanEvent.getEndElapsed();

        this.rpc = tSpanEvent.getRpc();
        this.serviceType = tSpanEvent.getServiceType();


        this.destinationId = tSpanEvent.getDestinationId();

        this.endPoint = tSpanEvent.getEndPoint();
        this.apiId = tSpanEvent.getApiId();

        if (tSpanEvent.isSetDepth()) {
            this.depth = tSpanEvent.getDepth();
        }
        
        if (tSpanEvent.isSetNextSpanId()) {
            this.nextSpanId = tSpanEvent.getNextSpanId();
        }
        
        setAnnotationList(tSpanEvent.getAnnotations());

        final TIntStringValue exceptionInfo = tSpanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            this.hasException = true;
            this.exceptionId = exceptionInfo.getIntValue();
            this.exceptionMessage = exceptionInfo.getStringValue();
        }
        
        if(tSpanEvent.isSetAsyncId()) {
            this.asyncId = tSpanEvent.getAsyncId();
        }
        
        if(tSpanEvent.isSetNextAsyncId()) {
            this.nextAsyncId = tSpanEvent.getNextAsyncId();
        }
        
        if(tSpanEvent.isSetAsyncSequence()) {
            this.asyncSequence = tSpanEvent.getAsyncSequence();
        }
    }

    public SpanEventBo(TSpanChunk spanChunk, TSpanEvent spanEvent) {
        if (spanChunk == null) {
            throw new NullPointerException("spanChunk must not be null");
        }
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }

        this.agentId = spanChunk.getAgentId();
        this.applicationId = spanChunk.getApplicationName();
        this.agentStartTime = spanChunk.getAgentStartTime();

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(spanChunk.getTransactionId());
        this.traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            traceAgentId = this.agentId;
        }
        this.traceAgentStartTime = transactionId.getAgentStartTime();
        this.traceTransactionSequence = transactionId.getTransactionSequence();

        this.spanId = spanChunk.getSpanId();
        this.sequence = spanEvent.getSequence();

        this.startElapsed = spanEvent.getStartElapsed();
        this.endElapsed = spanEvent.getEndElapsed();

        this.rpc = spanEvent.getRpc();
        this.serviceType = spanEvent.getServiceType();

        this.destinationId = spanEvent.getDestinationId();

        this.endPoint = spanEvent.getEndPoint();
        this.apiId = spanEvent.getApiId();

        if (spanEvent.isSetDepth()) {
            this.depth = spanEvent.getDepth();
        }

        if (spanEvent.isSetNextSpanId()) {
            this.nextSpanId = spanEvent.getNextSpanId();
        }

        setAnnotationList(spanEvent.getAnnotations());

        final TIntStringValue exceptionInfo = spanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            this.hasException = true;
            this.exceptionId = exceptionInfo.getIntValue();
            this.exceptionMessage = exceptionInfo.getStringValue();
        }
        
        if(spanEvent.isSetAsyncId()) {
            this.asyncId = spanEvent.getAsyncId();
        }
        
        if(spanEvent.isSetNextAsyncId()) {
            this.nextAsyncId = spanEvent.getNextAsyncId();
        }
        
        if(spanEvent.isSetAsyncSequence()) {
            this.asyncSequence = spanEvent.getAsyncSequence();
        }
    }



    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getAgentStartTime() {
        return this.agentStartTime;
    }

    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
    }

    public String getTraceAgentId() {
        return traceAgentId;
    }

    public void setTraceAgentId(String traceAgentId) {
        this.traceAgentId = traceAgentId;
    }

    public long getTraceAgentStartTime() {
        return traceAgentStartTime;
    }

    public void setTraceAgentStartTime(long traceAgentStartTime) {
        this.traceAgentStartTime = traceAgentStartTime;
    }

    public long getTraceTransactionSequence() {
        return traceTransactionSequence;
    }

    public void setTraceTransactionSequence(long traceTransactionSequence) {
        this.traceTransactionSequence = traceTransactionSequence;
    }

    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public long getSpanId() {
        return this.spanId;
    }

    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }

    public int getStartElapsed() {
        return startElapsed;
    }

    public void setStartElapsed(int startElapsed) {
        this.startElapsed = startElapsed;
    }

    public int getEndElapsed() {
        return endElapsed;
    }

    public void setEndElapsed(int endElapsed) {
        this.endElapsed = endElapsed;
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public short getServiceType() {
        return serviceType;
    }

    public void setServiceType(short serviceType) {
        this.serviceType = serviceType;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }


    public List<AnnotationBo> getAnnotationBoList() {
        return annotationBoList;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public long getNextSpanId() {
        return nextSpanId;
    }

    public void setNextSpanId(long nextSpanId) {
        this.nextSpanId = nextSpanId;
    }

    public void setAnnotationList(List<TAnnotation> annotations) {
        if (annotations == null) {
            return;
        }
        List<AnnotationBo> boList = new ArrayList<AnnotationBo>(annotations.size());
        for (TAnnotation ano : annotations) {
            boList.add(new AnnotationBo(ano));
        }
        this.annotationBoList = boList;
    }

    public void setAnnotationBoList(List<AnnotationBo> anoList) {
        if (anoList == null) {
            return;
        }
        this.annotationBoList = anoList;
    }
    
    public boolean isAsync() {
        return this.asyncId != -1;
    }
    
    public boolean hasException() {
        return hasException;
    }

    public int getExceptionId() {
        return exceptionId;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public int getAsyncId() {
        return asyncId;
    }

    public void setAsyncId(int asyncId) {
        this.asyncId = asyncId;
    }

    public int getNextAsyncId() {
        return nextAsyncId;
    }

    public void setNextAsyncId(int nextAsyncId) {
        this.nextAsyncId = nextAsyncId;
    }
    
    public short getAsyncSequence() {
        return asyncSequence;
    }

    public void setAsyncSequence(short asyncSequence) {
        this.asyncSequence = asyncSequence;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer(512);

        buffer.put(version);

        // buffer.put(mostTraceID);
        // buffer.put(leastTraceID);

        buffer.putPrefixedString(agentId);
        buffer.putPrefixedString(applicationId);
        buffer.putVar(agentStartTime);

        buffer.putVar(startElapsed);
        buffer.putVar(endElapsed);

        // don't need to put sequence because it is set at Qualifier
        // buffer.put(sequence);

        buffer.putPrefixedString(rpc);
        buffer.put(serviceType);
        buffer.putPrefixedString(endPoint);
        buffer.putPrefixedString(destinationId);
        buffer.putSVar(apiId);

        buffer.putSVar(depth);
        buffer.put(nextSpanId);

        if (hasException) {
            buffer.put(true);
            buffer.putSVar(exceptionId);
            buffer.putPrefixedString(exceptionMessage);
        } else {
            buffer.put(false);
        }

        writeAnnotation(buffer);
        buffer.putSVar(nextAsyncId);

        return buffer.getBuffer();
    }

    private void writeAnnotation(Buffer buffer) {
        AnnotationBoList annotationBo = new AnnotationBoList(this.annotationBoList);
        annotationBo.writeValue(buffer);
    }

    public int readValue(byte[] bytes, int offset, int length) {
        final int endOffset = offset + length;
        final Buffer buffer = new OffsetFixedBuffer(bytes, offset);

        this.version = buffer.readByte();

        // this.mostTraceID = buffer.readLong();
        // this.leastTraceID = buffer.readLong();

        this.agentId = buffer.readPrefixedString();
        this.applicationId = buffer.readPrefixedString();
        this.agentStartTime = buffer.readVarLong();

        this.startElapsed = buffer.readVarInt();
        this.endElapsed = buffer.readVarInt();

        // don't need to get sequence because it can be got at Qualifier
        // this.sequence = buffer.readShort();


        this.rpc = buffer.readPrefixedString();
        this.serviceType = buffer.readShort();
        this.endPoint = buffer.readPrefixedString();
        this.destinationId = buffer.readPrefixedString();
        this.apiId = buffer.readSVarInt();

        this.depth = buffer.readSVarInt();
        this.nextSpanId = buffer.readLong();

        this.hasException = buffer.readBoolean();
        if (hasException) {
            this.exceptionId = buffer.readSVarInt();
            this.exceptionMessage = buffer.readPrefixedString();
        }

        this.annotationBoList = readAnnotation(buffer);
        if(buffer.getOffset() < endOffset) {
            nextAsyncId = buffer.readSVarInt();            
        }
        
        return buffer.getOffset();
    }

    private List<AnnotationBo> readAnnotation(Buffer buffer) {
        AnnotationBoList annotationBoList = new AnnotationBoList();
        annotationBoList.readValue(buffer);
        return annotationBoList.getAnnotationBoList();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{version=");
        builder.append(version);
        builder.append(", agentId=");
        builder.append(agentId);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append(", agentStartTime=");
        builder.append(agentStartTime);
        builder.append(", traceAgentId=");
        builder.append(traceAgentId);
        builder.append(", traceAgentStartTime=");
        builder.append(traceAgentStartTime);
        builder.append(", traceTransactionSequence=");
        builder.append(traceTransactionSequence);
        builder.append(", spanId=");
        builder.append(spanId);
        builder.append(", sequence=");
        builder.append(sequence);
        builder.append(", startElapsed=");
        builder.append(startElapsed);
        builder.append(", endElapsed=");
        builder.append(endElapsed);
        builder.append(", rpc=");
        builder.append(rpc);
        builder.append(", serviceType=");
        builder.append(serviceType);
        builder.append(", destinationId=");
        builder.append(destinationId);
        builder.append(", endPoint=");
        builder.append(endPoint);
        builder.append(", apiId=");
        builder.append(apiId);
        builder.append(", annotationBoList=");
        builder.append(annotationBoList);
        builder.append(", depth=");
        builder.append(depth);
        builder.append(", nextSpanId=");
        builder.append(nextSpanId);
        builder.append(", hasException=");
        builder.append(hasException);
        builder.append(", exceptionId=");
        builder.append(exceptionId);
        builder.append(", exceptionMessage=");
        builder.append(exceptionMessage);
        builder.append(", exceptionClass=");
        builder.append(exceptionClass);
        builder.append(", asyncId=");
        builder.append(asyncId);
        builder.append(", nextAsyncId=");
        builder.append(nextAsyncId);
        builder.append("}");
        return builder.toString();
    }
}
