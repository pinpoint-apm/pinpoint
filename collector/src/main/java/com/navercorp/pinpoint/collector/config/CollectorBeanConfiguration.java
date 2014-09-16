package com.nhn.pinpoint.collector.config;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.io.TBaseLocator;
import com.nhn.pinpoint.thrift.io.TCommandRegistry;
import com.nhn.pinpoint.thrift.io.TCommandTypeVersion;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;

/**
 * @author koo.taejin <kr14910>
 */
@Configuration
public class CollectorBeanConfiguration {

	public static final int DEFAULT_SERIALIZER_MAX_SIZE = 1024 * 64;

	@Bean
	@Scope(value = "singleton")
	public SerializerFactory commandSerializerFactory() {
		TBaseLocator commandTbaseLocator = new TCommandRegistry(TCommandTypeVersion.getVersion(Version.VERSION));

		TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
		HeaderTBaseSerializerFactory serializerFactory = new HeaderTBaseSerializerFactory(true, DEFAULT_SERIALIZER_MAX_SIZE, protocolFactory, commandTbaseLocator);

		ThreadLocalHeaderTBaseSerializerFactory threadLocalSerializerFactory = new ThreadLocalHeaderTBaseSerializerFactory(serializerFactory);
		return threadLocalSerializerFactory;
	}

	@Bean
	@Scope(value = "singleton")
	public DeserializerFactory commandDeserializerFactory() {
		TBaseLocator commandTbaseLocator = new TCommandRegistry(TCommandTypeVersion.getVersion(Version.VERSION));

		TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
		HeaderTBaseDeserializerFactory deserializerFactory = new HeaderTBaseDeserializerFactory(protocolFactory, commandTbaseLocator);

		ThreadLocalHeaderTBaseDeserializerFactory threadLocalDeserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory(deserializerFactory);
		return threadLocalDeserializerFactory;
	}

}
