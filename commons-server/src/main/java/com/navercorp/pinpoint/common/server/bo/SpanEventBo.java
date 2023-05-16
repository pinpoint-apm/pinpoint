/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanEventBo implements Event {

    // version 0 means that the type of prefix's size is int

    private byte version = 0;

    private short sequence;

    private int startElapsed;
    private int endElapsed;

    // private String rpc;
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

    private int nextAsyncId = -1;

    @Deprecated
    private int asyncId = -1;
    @Deprecated
    private short asyncSequence = -1;

    public SpanEventBo() {
    }


    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
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

    @Deprecated
    public String getRpc() {
        return null;
    }

    @Deprecated
    public void setRpc(String rpc) {
//        this.rpc = rpc;
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


    public void setAnnotationBoList(List<AnnotationBo> annotationList) {
        if (annotationList == null) {
            return;
        }
        this.annotationBoList = annotationList;
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

    public void setExceptionInfo(int exceptionId, String exceptionMessage) {
        this.hasException = true;
        this.exceptionId = exceptionId;
        this.exceptionMessage = exceptionMessage;
    }


    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public int getNextAsyncId() {
        return nextAsyncId;
    }

    public void setNextAsyncId(int nextAsyncId) {
        this.nextAsyncId = nextAsyncId;
    }


    @Deprecated
    public int getAsyncId() {
        return asyncId;
    }

    @Deprecated
    public void setAsyncId(int asyncId) {
        this.asyncId = asyncId;
    }

    @Deprecated
    public short getAsyncSequence() {
        return asyncSequence;
    }

    @Deprecated
    public void setAsyncSequence(short asyncSequence) {
        this.asyncSequence = asyncSequence;
    }

    private boolean isDeprecatedAsyncFieldsSet() {
        return asyncId != -1 || asyncSequence != -1;
    }

    @Override
    public String toString() {
        return "SpanEventBo{" +
                "version=" + version +
                ", sequence=" + sequence +
                ", startElapsed=" + startElapsed +
                ", endElapsed=" + endElapsed +
                ", serviceType=" + serviceType +
                ", destinationId='" + destinationId + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", apiId=" + apiId +
                ", annotationBoList=" + annotationBoList +
                ", depth=" + depth +
                ", nextSpanId=" + nextSpanId +
                ", hasException=" + hasException +
                ", exceptionId=" + exceptionId +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", exceptionClass='" + exceptionClass + '\'' +
                ", nextAsyncId=" + nextAsyncId +
                ", asyncId=" + asyncId +
                ", asyncSequence=" + asyncSequence +
                '}';
    }

    public static class Builder {
        private int version = 0;

        private short sequence;

        private int startElapsed;
        private int endElapsed;

        //    private String rpc;
        private short serviceType;

        private String destinationId;
        private String endPoint;
        private int apiId;

        private List<AnnotationBo> annotationBoList = new ArrayList<>();

        private int depth = -1;
        private long nextSpanId = -1;

        private int nextAsyncId = -1;

        public Builder setVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder setSequence(short sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder setStartElapsed(int startElapsed) {
            this.startElapsed = startElapsed;
            return this;
        }

        public Builder setEndElapsed(int endElapsed) {
            this.endElapsed = endElapsed;
            return this;
        }

        public Builder setServiceType(short serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder setDestinationId(String destinationId) {
            this.destinationId = destinationId;
            return this;
        }

        public Builder setEndPoint(String endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public Builder setApiId(int apiId) {
            this.apiId = apiId;
            return this;
        }

        public Builder addAnnotationBo(AnnotationBo e) {
            this.annotationBoList.add(e);
            return this;
        }

        public Builder setDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder setNextSpanId(long nextSpanId) {
            this.nextSpanId = nextSpanId;
            return this;
        }

        public Builder setNextAsyncId(int nextAsyncId) {
            this.nextAsyncId = nextAsyncId;
            return this;
        }

        public SpanEventBo build() {
            SpanEventBo result = new SpanEventBo();
            result.setVersion((byte) this.version);
            result.setSequence(this.sequence);
            result.setStartElapsed(this.startElapsed);
            result.setEndElapsed(this.endElapsed);
            result.setServiceType(this.serviceType);
            result.setDestinationId(this.destinationId);
            result.setEndPoint(this.endPoint);
            result.setApiId(this.apiId);
            result.setAnnotationBoList(this.annotationBoList);
            result.setDepth(this.depth);
            result.setNextSpanId(this.nextSpanId);
            result.setNextAsyncId(this.nextAsyncId);
            return result;
        }

    }
}
