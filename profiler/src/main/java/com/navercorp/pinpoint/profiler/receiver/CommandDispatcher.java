package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.nhn.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.nhn.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.nhn.pinpoint.rpc.util.AssertUtils;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.io.TCommandRegistry;
import com.nhn.pinpoint.thrift.io.TCommandTypeVersion;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;
import com.nhn.pinpoint.thrift.util.SerializationUtils;

public class CommandDispatcher implements MessageListener, ServerStreamChannelMessageListener  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerCommandServiceRegistry commandServiceRegistry = new ProfilerCommandServiceRegistry();
    
    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    public CommandDispatcher() {
        this(Version.VERSION);
    }

    public CommandDispatcher(String pinpointVersion) {
        this(pinpointVersion, HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE);
    }

    public CommandDispatcher(String pinpointVersion, int serializationMaxSize) {
        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        TCommandRegistry commandTbaseRegistry = new TCommandRegistry(TCommandTypeVersion.getVersion(pinpointVersion));
        
        SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(true, serializationMaxSize, protocolFactory, commandTbaseRegistry);
        this.serializerFactory = wrappedThreadLocalSerializerFactory(serializerFactory);
        AssertUtils.assertNotNull(this.serializerFactory);

        DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new HeaderTBaseDeserializerFactory(protocolFactory, commandTbaseRegistry);
        this.deserializerFactory = wrappedThreadLocalDeserializerFactory(deserializerFactory);
        AssertUtils.assertNotNull(this.deserializerFactory);
    }

    @Override
    public void handleSend(SendPacket sendPacket, Channel channel) {
        logger.info("MessageReceive {} {}", sendPacket, channel);
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, Channel channel) {
        logger.info("MessageReceive {} {}", requestPacket, channel);

        TBase<?, ?> request = SerializationUtils.deserialize(requestPacket.getPayload(), deserializerFactory, null);
        
        TBase response = null;
        if (request == null) {
            TResult tResult = new TResult(false);
            tResult.setMessage("Unsupported Type.");
            
            response = tResult;
        } else {
            ProfilerRequestCommandService service = commandServiceRegistry.getRequestService(request);
            
            if (service == null) {
                TResult tResult = new TResult(false);
                tResult.setMessage("Unsupported Listener.");

                response = tResult;
            } else {
                response = service.requestCommandService(request);
            }
        }
        
        byte[] payload = SerializationUtils.serialize(response, serializerFactory, null);
        if (payload != null) {
            channel.write(new ResponsePacket(requestPacket.getRequestId(), payload));
        }       
    }

    @Override
    public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
        logger.info("MessageReceived handleStreamCreate {} {}", packet, streamChannelContext);

        TBase<?, ?> request = SerializationUtils.deserialize(packet.getPayload(), deserializerFactory, null);
        
        ProfilerStreamCommandService service = commandServiceRegistry.getStreamService(request);
        if (service == null) {
            return StreamCreateFailPacket.PACKET_UNSUPPORT;
        }
        
        service.streamCommandService(request, streamChannelContext);
        
        return StreamCreatePacket.SUCCESS;
    }

    @Override
    public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
    }

    public boolean registerCommandService(ProfilerCommandService commandService) {
        return this.commandServiceRegistry.addService(commandService);
    }

    private SerializerFactory<HeaderTBaseSerializer> wrappedThreadLocalSerializerFactory(SerializerFactory<HeaderTBaseSerializer> serializerFactory) {
        return new ThreadLocalHeaderTBaseSerializerFactory<HeaderTBaseSerializer>(serializerFactory);
    }

    private DeserializerFactory<HeaderTBaseDeserializer> wrappedThreadLocalDeserializerFactory(DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
        return new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(deserializerFactory);
    }
	
}
