/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.header.ByteArrayHeaderWriter;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderWriter;
import com.navercorp.pinpoint.io.header.InvalidHeaderException;
import com.navercorp.pinpoint.io.request.FlinkRequest;
import com.navercorp.pinpoint.io.util.TypeLocator;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;

/**
 * @author minwoo.jung
 */
public class FlinkHeaderTBaseSerializer {

    private final ResettableByteArrayOutputStream baos;
    private final TProtocol protocol;

    /**
     * Create a new HeaderTBaseSerializer.
     *
     * @param bos
     * @param protocolFactory
     */
    public FlinkHeaderTBaseSerializer(ResettableByteArrayOutputStream bos, TProtocolFactory protocolFactory) {
        this.baos = Assert.requireNonNull(bos, "ResettableByteArrayOutputStream must not be null.");

        Assert.requireNonNull(protocolFactory, "TProtocolFactory must not be null.");
        TIOStreamTransport transport = new TIOStreamTransport(bos);
        this.protocol = protocolFactory.getProtocol(transport);
    }

    public byte[] serialize(FlinkRequest flinkRequest) throws TException {
        baos.reset();
        writeHeader(flinkRequest.getHeader());
        flinkRequest.getData().write(protocol);
        return baos.toByteArray();
    }

    private void writeHeader(Header header) {
        try {
            HeaderWriter headerWriter = new ByteArrayHeaderWriter(header);
            byte[] headerBytes = headerWriter.writeHeader();
            baos.write(headerBytes);
        } catch (Exception e) {
            throw new InvalidHeaderException("can not write header.", e);
        }
    }


}
