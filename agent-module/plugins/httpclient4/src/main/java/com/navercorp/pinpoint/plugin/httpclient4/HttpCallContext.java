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

package com.navercorp.pinpoint.plugin.httpclient4;

/**
 * @author minwoo.jung
 */
public class HttpCallContext {
    private int statusCode;
    private String host = "";
    private int port;

    private long readBeginTime;
    private long readEndTime;
    private boolean readFail;
    
    private long writeBeginTime;
    private long writeEndTime;
    private boolean writeFail;

    public void setReadBeginTime(long readBeginTime) {
        this.readBeginTime = readBeginTime;
    }

    public void setReadEndTime(long readEndTime) {
        this.readEndTime = readEndTime;
    }
    
    public boolean isReadFail() {
        return readFail;
    }

    public void setReadFail(boolean readFail) {
        this.readFail = readFail;
    }

    public void setWriteBeginTime(long writeBeginTime) {
        this.writeBeginTime = writeBeginTime;
    }

    public void setWriteEndTime(long writeEndTime) {
        this.writeEndTime = writeEndTime;
    }
    
    public boolean isWriteFail() {
        return writeFail;
    }

    public void setWriteFail(boolean writeFail) {
        this.writeFail = writeFail;
    }

    public long getWriteElapsedTime() {
        long result = writeEndTime - writeBeginTime;
        return result > 0 ? result : 0;
    }

    public long getReadElapsedTime() {
        long result = readEndTime - readBeginTime;
        return result > 0 ? result : 0;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{statusCode=");
        builder.append(statusCode);
        builder.append(", host=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append(", readBeginTime=");
        builder.append(readBeginTime);
        builder.append(", readEndTime=");
        builder.append(readEndTime);
        builder.append(", readFail=");
        builder.append(readFail);
        builder.append(", writeBeginTime=");
        builder.append(writeBeginTime);
        builder.append(", writeEndTime=");
        builder.append(writeEndTime);
        builder.append(", writeFail=");
        builder.append(writeFail);
        builder.append("}");
        return builder.toString();
    }
}
