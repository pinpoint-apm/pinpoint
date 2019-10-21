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

package com.navercorp.pinpoint.profiler.receiver;

import com.google.inject.Inject;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class CommandDispatcher extends ServerStreamChannelMessageHandler implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerCommandServiceLocator<TBase<?, ?>, TBase<?, ?>> commandServiceLocator;

    private final CommandHeaderTBaseSerializerFactory commandHeaderTBaseSerializerFactory = CommandHeaderTBaseSerializerFactory.getDefaultInstance();
    private final CommandHeaderTBaseDeserializerFactory commandHeaderTBaseDeserializerFactory = CommandHeaderTBaseDeserializerFactory.getDefaultInstance();

    @Inject
    public CommandDispatcher(ProfilerCommandServiceLocator<TBase<?, ?>, TBase<?, ?>> commandServiceLocator) {
        this.commandServiceLocator = Assert.requireNonNull(commandServiceLocator, "commandServiceLocator");
    }

    @Override
    public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

        final Message<TBase<?, ?>> message = SerializationUtils.deserialize(requestPacket.getPayload(), commandHeaderTBaseDeserializerFactory, null);
        if (logger.isDebugEnabled()) {
            logger.debug("handleRequest request:{}, remote:{}", message, pinpointSocket.getRemoteAddress());
        }

        final TBase response = processRequest(message);

        final byte[] payload = SerializationUtils.serialize(response, commandHeaderTBaseSerializerFactory, null);
        if (payload != null) {
            pinpointSocket.response(requestPacket.getRequestId(), payload);
        }
    }

    private TBase<?, ?> processRequest(Message<TBase<?, ?>> message) {
        if (message == null) {
            final TResult tResult = new TResult(false);
            tResult.setMessage("Unsupported ServiceTypeInfo.");

            return tResult;
        }

        final short type = message.getHeader().getType();
        final ProfilerRequestCommandService<TBase<?, ?>, TBase<?, ?>> service = commandServiceLocator.getRequestService(type);
        if (service == null) {
            TResult tResult = new TResult(false);
            tResult.setMessage("Can't find suitable service(" + message + ").");

            return tResult;
        }

        final TBase<?, ?> request = message.getData();
        final TBase<?, ?> tResponse = service.requestCommandService(request);
        return tResponse;
    }

    @Override
    public StreamCode handleStreamCreatePacket(ServerStreamChannel streamChannel, StreamCreatePacket packet) {
        logger.info("handleStreamCreatePacket() streamChannel:{}, packet:{}", streamChannel, packet);

        final Message<TBase<?, ?>> message = SerializationUtils.deserialize(packet.getPayload(), commandHeaderTBaseDeserializerFactory, null);
        if (message == null) {
            return StreamCode.TYPE_UNKNOWN;
        }

        final short type = message.getHeader().getType();
        final ProfilerStreamCommandService<TBase<?, ?>> service = commandServiceLocator.getStreamService(type);
        if (service == null) {
            return StreamCode.TYPE_UNSUPPORT;
        }

        final TBase<?, ?> request = message.getData();
        return service.streamCommandService(request, streamChannel);
    }

    @Override
    public void handleStreamClosePacket(ServerStreamChannel streamChannel, StreamClosePacket packet) {
        logger.info("handleStreamClosePacket() streamChannel:{}, packet:{}", streamChannel, packet);
    }

    public Set<Short> getRegisteredCommandServiceCodes() {
        return commandServiceLocator.getCommandServiceCodes();
    }

    public void close() {
        logger.info("close() started");

        Set<Short> commandServiceCodes = commandServiceLocator.getCommandServiceCodes();
        for (Short commandServiceCode : commandServiceCodes) {
            ProfilerCommandService service = commandServiceLocator.getService(commandServiceCode);
            if (service instanceof Closeable) {
                try {
                    ((Closeable) service).close();
                } catch (Exception e) {
                    logger.warn("failed to close for CommandService:{}. message:{}", service, e.getMessage());
                }
            }
        }

        logger.info("close() completed");
    }

    @Override
    public String toString() {
        return "CommandDispatcher{" + commandServiceLocator.getCommandServiceCodes() + '}';
    }

}
