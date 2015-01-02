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

package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author koo.taejin
 */
public final class CommandHeaderTBaseSerializerFactory implements SerializerFactory<HeaderTBaseSerializer> {

    public static final int DEFAULT_SERIALIZER_MAX_SIZE = 1024 * 64

	private final SerializerFactory<HeaderTBaseSerializer> fact    ry;

	public CommandHeaderTBaseSerializerFactory(String ve       sion) {
		this(version, DEFAULT_SERIALI        R_MAX_SIZE);
	}

	public CommandHeaderTBaseSerializerFactory(String version, int       outputStreamSize) {
		TBaseLocator commandTbaseLocator = new TCommandRegistry(TCommandTypeVers       on.getVersion(version));

		TProtocolFactory protocolFactory =       new TCompactProtocol.Factory();
		HeaderTBaseSerializerFactory serializerFactory = new HeaderTBaseSerializerFactory(true, outputStreamSize, p       otocolFactory, commandTbaseLocator);

		this.factory = new ThreadLocalHeaderTBaseSerializerFactor        HeaderT    aseSerializer>(serializerFactory);
	}

	@Overri       e
	public HeaderTBaseSerializer cre    teSerializer() {
		return this.factory.createSerializer();
	}

}
