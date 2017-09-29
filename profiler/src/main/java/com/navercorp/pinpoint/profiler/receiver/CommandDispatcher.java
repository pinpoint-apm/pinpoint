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

package com.navercorp.pinpoint.profiler.receiver;

import com.google.inject.Inject;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Taejin Koo
 */
public class CommandDispatcher implements MessageListener, ServerStreamChannelMessageListener  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerCommandServiceLocator commandServiceLocator;

    @Inject
    public CommandDispatcher(ProfilerCommandServiceLocator commandServiceLocator) {
        if (commandServiceLocator == null) {
            throw new NullPointerException("commandServiceLocator must not be null");
        }

        this.commandServiceLocator = commandServiceLocator;
    }

    @Override
    public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

        final TBase<?, ?> request = SerializationUtils.deserialize(requestPacket.getPayload(), CommandSerializer.DESERIALIZER_FACTORY, null);
        logger.debug("handleRequest request:{}, remote:{}", request, pinpointSocket.getRemoteAddress());

        TBase response;
        if (request == null) {
            final TResult tResult = new TResult(false);
            tResult.setMessage("Unsupported ServiceTypeInfo.");

            response = tResult;
        } else {
            final ProfilerRequestCommandService service = commandServiceLocator.getRequestService(request);
            if (service == null) {
                TResult tResult = new TResult(false);
                tResult.setMessage("Can't find suitable service(" + request + ").");

                response = tResult;
            } else {
                response = service.requestCommandService(request);
            }
        }

        final byte[] payload = SerializationUtils.serialize(response, CommandSerializer.SERIALIZER_FACTORY, null);
        if (payload != null) {
            pinpointSocket.response(requestPacket, payload);
        }
    }

    @Override
    public StreamCode handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
        logger.info("MessageReceived handleStreamCreate {} {}", packet, streamChannelContext);

        final TBase<?, ?> request = SerializationUtils.deserialize(packet.getPayload(), CommandSerializer.DESERIALIZER_FACTORY, null);
        if (request == null) {
            return StreamCode.TYPE_UNKNOWN;
        }
        
        final ProfilerStreamCommandService service = commandServiceLocator.getStreamService(request);
        if (service == null) {
            return StreamCode.TYPE_UNSUPPORT;
        }
        
        return service.streamCommandService(request, streamChannelContext);
    }

    @Override
    public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
    }

    public Set<Short> getRegisteredCommandServiceCodes() {
        return commandServiceLocator.getCommandServiceCodes();
    }

}
