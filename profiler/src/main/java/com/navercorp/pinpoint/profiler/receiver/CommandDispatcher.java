package com.nhn.pinpoint.profiler.receiver;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.profiler.receiver.service.EchoService;
import com.nhn.pinpoint.profiler.receiver.service.ThreadDumpService;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.util.AssertUtils;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.io.TBaseLocator;
import com.nhn.pinpoint.thrift.io.TCommandRegistry;
import com.nhn.pinpoint.thrift.io.TCommandTypeVersion;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;

public class CommandDispatcher implements MessageListener  {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ProfilerCommandServiceLocator locator;

	private final SerializerFactory serializerFactory;
	private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

	public CommandDispatcher(Builder builder) {
		ProfilerCommandServiceRegistry registry = new ProfilerCommandServiceRegistry();
		for (ProfilerCommandService service : builder.serviceList) {
			registry.addService(service);
		}
		this.locator = registry;
		
		SerializerFactory serializerFactory = new HeaderTBaseSerializerFactory(true, builder.serializationMaxSize, builder.protocolFactory, builder.commandTbaseLocator);
		this.serializerFactory = wrappedThreadLocalSerializerFactory(serializerFactory);
		AssertUtils.assertNotNull(this.serializerFactory);
		
		DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new HeaderTBaseDeserializerFactory(builder.protocolFactory, builder.commandTbaseLocator);
		this.deserializerFactory = wrappedThreadLocalDeserializerFactory(deserializerFactory);
		AssertUtils.assertNotNull(this.deserializerFactory);
	}

	private SerializerFactory wrappedThreadLocalSerializerFactory(SerializerFactory serializerFactory) {
		return new ThreadLocalHeaderTBaseSerializerFactory(serializerFactory);
	}
	
	private DeserializerFactory<HeaderTBaseDeserializer> wrappedThreadLocalDeserializerFactory(DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
		return new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(deserializerFactory);
	}
	
	@Override
	public void handleSend(SendPacket sendPacket, Channel channel) {
		logger.info("MessageReceive {} {}", sendPacket, channel);
	}

	
	@Override
	public void handleRequest(RequestPacket requestPacket, Channel channel) {
		logger.info("MessageReceive {} {}", requestPacket, channel);

		TBase<?, ?> request = deserialize(requestPacket.getPayload());
		
		TBase response = null;
		if (request == null) {
			TResult tResult = new TResult(false);
			tResult.setMessage("Unsupported Type.");
			
			response = tResult;
		} else {
			ProfilerRequestCommandService service = locator.getRequestService(request);
			
			if (service == null) {
				TResult tResult = new TResult(false);
				tResult.setMessage("Unsupported Listener.");

				response = tResult;
			} else {
				response = service.requestCommandService(request);
			}
		}
		
		byte[] payload = serialize(response);
		if (payload != null) {
			channel.write(new ResponsePacket(requestPacket.getRequestId(), payload));
		}		
	}

	private byte[] serialize(TBase result) {
		if (result == null) {
			logger.warn("tBase may not be null.");
			return null;
		}
		
    	try {
			HeaderTBaseSerializer serializer = serializerFactory.createSerializer();
			byte[] payload = serializer.serialize(result);
			return payload;
		} catch (TException e) {
			logger.warn(e.getMessage(), e);
		}
    	
    	return null;
	}
	
	private TBase deserialize(byte[] payload) {
		if (payload == null) {
			logger.warn("Payload may not be null.");
			return null;
		}
		
    	try {
			final HeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();
			TBase<?, ?> tBase = deserializer.deserialize(payload);
			return tBase;
		} catch (TException e) {
			logger.warn(e.getMessage(), e);
		}
    	
    	return null;
	}
	
	public static class Builder {
		private List<ProfilerCommandService> serviceList = new ArrayList<ProfilerCommandService>();

		private int serializationMaxSize = HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE;
		private TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
		private TBaseLocator commandTbaseLocator = new TCommandRegistry(TCommandTypeVersion.getVersion(Version.VERSION));
		
		public Builder() {
			serviceList.add(new ThreadDumpService());
			serviceList.add(new EchoService());
		}
		
		public Builder addService(ProfilerCommandService service) {
			serviceList.add(service);
			return this;
		}

		public Builder setProtocolFactory(TProtocolFactory protocolFactory) {
			this.protocolFactory = protocolFactory;
			return this;
		}

		public Builder setCommandTbaseLocator(TBaseLocator commandTbaseLocator) {
			this.commandTbaseLocator = commandTbaseLocator;
			return this;
		}

		public void setSerializationMaxSize(int serializationMaxSize) {
			this.serializationMaxSize = serializationMaxSize;
		}

		public CommandDispatcher build() {
			AssertUtils.assertNotNull(protocolFactory, "protocolFactory may note be null.");
			AssertUtils.assertNotNull(commandTbaseLocator, "commandTbaseLocator may note be null.");
			AssertUtils.assertTrue(serializationMaxSize > 0, "serializationMaxSize must grater than zero.");
			AssertUtils.assertTrue(serviceList.size() > 0, "serializationMaxSize must grater than zero.");
			
			return new CommandDispatcher(this);
		}
		
	}
	
}
