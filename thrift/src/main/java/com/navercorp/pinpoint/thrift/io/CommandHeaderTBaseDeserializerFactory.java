package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author koo.taejin <kr14910>
 */
public final class CommandHeaderTBaseDeserializerFactory implements DeserializerFactory<HeaderTBaseDeserializer> {

	private final DeserializerFactory<HeaderTBaseDeserializer> factory;

	public CommandHeaderTBaseDeserializerFactory(String version) {
		System.out.println(version);
		
		TBaseLocator commandTbaseLocator = new TCommandRegistry(TCommandTypeVersion.getVersion(version));

		TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
		HeaderTBaseDeserializerFactory deserializerFactory = new HeaderTBaseDeserializerFactory(protocolFactory, commandTbaseLocator);

		this.factory = new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(deserializerFactory);
	}

	@Override
	public HeaderTBaseDeserializer createDeserializer() {
		return this.factory.createDeserializer();
	}

}
