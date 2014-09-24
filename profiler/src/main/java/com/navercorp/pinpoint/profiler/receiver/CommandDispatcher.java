package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.profiler.receiver.bo.EchoBO;
import com.nhn.pinpoint.profiler.receiver.bo.ThreadDumpBO;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandEcho;
import com.nhn.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.io.TBaseLocator;
import com.nhn.pinpoint.thrift.io.TCommandRegistry;
import com.nhn.pinpoint.thrift.io.TCommandTypeVersion;

/**
 * @author koo.taejin
 */
public class CommandDispatcher implements MessageListener {

	// 일단은 현재 스레드가 워커스레드로 되는 것을 등록 (이후에 변경하자.)
	
	private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();
	private static final TBaseLocator commandTbaseLocator = new TCommandRegistry(TCommandTypeVersion.getVersion(Version.VERSION));
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 여기만 따로 TBaseLocator를 상속받아 만들어주는 것이 좋을듯 
	
	private final TBaseBOLocator locator;

    private final SerializerFactory serializerFactory = new HeaderTBaseSerializerFactory(true, HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE, DEFAULT_PROTOCOL_FACTORY, commandTbaseLocator);
	private final HeaderTBaseDeserializerFactory deserializerFactory = new HeaderTBaseDeserializerFactory(DEFAULT_PROTOCOL_FACTORY, commandTbaseLocator);
	
	public CommandDispatcher() {
		TBaseBORegistry registry = new TBaseBORegistry();
		registry.addBO(TCommandThreadDump.class, new ThreadDumpBO());
		registry.addBO(TCommandEcho.class, new EchoBO());

		this.locator = registry;
	}

	@Override
	public void handleRequest(RequestPacket packet, Channel channel) {
		logger.info("MessageReceive {} {}", packet, channel);

		TBase<?, ?> request = deserialize(packet.getPayload());
		
		TBase response = null;
		if (request == null) {
			TResult tResult = new TResult(false);
			tResult.setMessage("Unsupported Type.");
			
			response = tResult;
		} else {
			TBaseRequestBO bo = locator.getRequestBO(request);
			
			if (bo == null) {
				TResult tResult = new TResult(false);
				tResult.setMessage("Unsupported Listener.");

				response = tResult;
			} else {
				response = bo.handleRequest(request);
			}
		}
		
		byte[] payload = serialize(response);
		if (payload != null) {
			channel.write(new ResponsePacket(packet.getRequestId(), payload));
		}
	}

	@Override
	public void handleSend(SendPacket packet, Channel channel) {
		logger.info("MessageReceive {} {}", packet, channel);
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

}
