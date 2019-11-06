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

package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.*;
import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class PinpointRouteResponseTest {

    TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
    TypeLocator<TBase<?, ?>> commandTbaseRegistry = TCommandRegistry.build(TCommandTypeVersion.getVersion("1.5.0-SNAPSHOT"));

    SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(true, 10000, protocolFactory, commandTbaseRegistry);
    DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new HeaderTBaseDeserializerFactory(protocolFactory, commandTbaseRegistry);

    @Test
    public void routeResponseTest1() throws Exception {
        HeaderTBaseSerializer serializer = serializerFactory.createSerializer();

        byte[] contents = serializer.serialize(createCommandEcho("echo"));

        DefaultPinpointRouteResponse response = new DefaultPinpointRouteResponse(contents);
        response.parse(deserializerFactory);

        Assert.assertEquals(TRouteResult.UNKNOWN, response.getRouteResult());
        Assert.assertTrue(response.getResponse() instanceof TCommandEcho);
    }

    @Test
    public void routeResponseTest2() throws Exception {
        HeaderTBaseSerializer serializer = serializerFactory.createSerializer();

        byte[] contents = serializer.serialize(createCommandEcho("echo"));
        byte[] responsePayload = serializer.serialize(wrapResponse(TRouteResult.OK, contents));

        DefaultPinpointRouteResponse response = new DefaultPinpointRouteResponse(responsePayload);
        response.parse(deserializerFactory);

        Assert.assertEquals(TRouteResult.OK, response.getRouteResult());
        Assert.assertTrue(response.getResponse() instanceof TCommandEcho);
        Assert.assertNull(response.getMessage());
    }

    @Test
    public void routeResponseTest3() throws Exception {
        HeaderTBaseSerializer serializer = serializerFactory.createSerializer();

        String message = "hello";
        byte[] responsePayload = serializer.serialize(wrapResponse(TRouteResult.OK, new byte[1], message));

        DefaultPinpointRouteResponse response = new DefaultPinpointRouteResponse(responsePayload);
        response.parse(deserializerFactory);

        Assert.assertEquals(TRouteResult.OK, response.getRouteResult());
        Assert.assertNull(response.getResponse());
        Assert.assertEquals(message, response.getMessage());
    }

    private TCommandEcho createCommandEcho(String message) {
        TCommandEcho echo = new TCommandEcho(message);
        return echo;
    }

    private TCommandTransferResponse wrapResponse(TRouteResult routeResult, byte[] payload) {
        return wrapResponse(routeResult, payload, null);
    }

    private TCommandTransferResponse wrapResponse(TRouteResult routeResult, byte[] payload, String message) {
        TCommandTransferResponse response = new TCommandTransferResponse();
        response.setRouteResult(routeResult);
        response.setPayload(payload);

        if (message != null) {
            response.setMessage(message);
        }

        return response;
    }


}
