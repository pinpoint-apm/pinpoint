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

import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.thrift.TBase;

/**
 * @author Taejin Koo
 */
public class DefaultPinpointRouteResponse implements PinpointRouteResponse {

    private final byte[] payload;

    private TRouteResult routeResult;
    private TBase response;
    private String message;

    private boolean isParsed;

    public DefaultPinpointRouteResponse(byte[] payload) {
        this.payload = payload;
    }

    public void parse(DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory) {
        if (!isParsed) {
            if (ArrayUtils.isEmpty(payload)) {
                routeResult = TRouteResult.EMPTY_RESPONSE;
                return;
            }

            TBase<?, ?> object = deserialize(commandDeserializerFactory, payload, null);

            if (object == null) {
                routeResult = TRouteResult.NOT_SUPPORTED_RESPONSE;
            } else if (object instanceof  TCommandTransferResponse) {
                TCommandTransferResponse commandResponse = (TCommandTransferResponse) object;
                TRouteResult routeResult = commandResponse.getRouteResult();
                if (routeResult == null) {
                    this.routeResult = TRouteResult.UNKNOWN;
                } else {
                    this.routeResult = routeResult;
                }

                response = deserialize(commandDeserializerFactory, commandResponse.getPayload(), null);
                message = commandResponse.getMessage();
            } else {
                routeResult = TRouteResult.UNKNOWN;
                response = object;
            }
            isParsed = true;
        }
    }

    @Override
    public TRouteResult getRouteResult() {
        assertParsed();
        return routeResult;
    }

    @Override
    public TBase getResponse() {
        assertParsed();
        return response;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public <R extends TBase> R getResponse(Class<R> clazz) {
        TBase response = getResponse();

        if (clazz.isInstance(response)) {
            return (R) response;
        }

        throw new ClassCastException("Not expected " + clazz + " type.");
    }

    @Override
    public <R extends TBase> R getResponse(Class<R> clazz, R defaultValue) {
        TBase response = getResponse();

        if (clazz.isInstance(response)) {
            return (R) response;
        }

        return defaultValue;
    }

    private void assertParsed() {
        if (!isParsed) {
            throw new IllegalStateException("not yet parsed.");
        }
    }

    private TBase<?, ?> deserialize(DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory, byte[] objectData, Message<TBase<?, ?>> defaultValue) {
        final Message<TBase<?, ?>> message = SerializationUtils.deserialize(objectData, commandDeserializerFactory, defaultValue);
        if (message == null) {
            return null;
        }
        return message.getData();
    }


}
