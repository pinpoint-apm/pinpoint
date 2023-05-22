package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.thrift.TBase;

import java.util.Objects;

public class RouteResponseParser {

    private final DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public RouteResponseParser(DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory) {
        this.commandDeserializerFactory = Objects.requireNonNull(commandDeserializerFactory, "commandDeserializerFactory");
    }

    public PinpointRouteResponse parse(byte[] payload) {
        if (ArrayUtils.isEmpty(payload)) {
            return new DefaultPinpointRouteResponse(TRouteResult.EMPTY_RESPONSE);
        }

        final TBase<?, ?> object = deserialize(commandDeserializerFactory, payload, null);
        if (object == null) {
            return new DefaultPinpointRouteResponse(TRouteResult.NOT_SUPPORTED_RESPONSE);
        } else if (object instanceof TCommandTransferResponse) {
            final TCommandTransferResponse commandResponse = (TCommandTransferResponse) object;
            TRouteResult routeResult = commandResponse.getRouteResult();
            if (routeResult == null) {
                routeResult = TRouteResult.UNKNOWN;
            }

            TBase<?, ?> response = deserialize(commandDeserializerFactory, commandResponse.getPayload(), null);
            String message = commandResponse.getMessage();
            return new DefaultPinpointRouteResponse(routeResult, response, message);
        } else {
            return new DefaultPinpointRouteResponse(TRouteResult.UNKNOWN, object, null);
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
