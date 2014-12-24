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
import java.net.DatagramPacket;

/**
 * byte arrays position
 * 
 * @author jaehong.kim
 *
 */
public class TBaseStreamNode implements ByteArrayOutput {

    private ByteArrayOutputStreamTransport transport;
    private int beginPosition;
    private int endPosition;
    private String className;

    public TBaseStreamNode(final ByteArrayOutputStreamTransport transport) {
        this.transport = transport;
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
        out.write(transport.getBuffer(), beginPosition, size());
    }

    public void writeTo(DatagramPacket packet) {
        packet.setData(transport.getBuffer(), beginPosition, size());
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