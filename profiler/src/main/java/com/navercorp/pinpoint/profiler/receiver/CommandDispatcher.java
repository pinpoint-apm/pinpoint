package com.navercorp.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.TCommandRegistry;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

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
