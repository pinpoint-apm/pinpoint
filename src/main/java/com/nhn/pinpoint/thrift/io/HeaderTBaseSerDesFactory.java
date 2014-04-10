package com.nhn.pinpoint.thrift.io;

import java.io.ByteArrayOutputStream;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author koo.taejin
 */
public final class HeaderTBaseSerDesFactory {

	private static final boolean DEFAULT_SAFE_GURANTEED = true;
	
	// 요기 두는게 이상하지만 Constants 하나 만드는것도 일단은 부담 
	// 일단 주석
	public static final int DEFAULT_SAFETY_GURANTEED_MAX_SERIALIZE_DATA_SIZE = 1024 * 4;
	public static final int DEFAULT_SAFETY_NOT_GURANTEED_MAX_SERIALIZE_DATA_SIZE = 1024 * 64;
			
	private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();

	private HeaderTBaseSerDesFactory() {
	}

	// Serializer 생성
	public static HeaderTBaseSerializer getSerializer(int outputStreamSize) {
		return getSerializer(DEFAULT_SAFE_GURANTEED, outputStreamSize, new TCompactProtocol.Factory(), DEFAULT_TBASE_LOCATOR);
	}

	public static HeaderTBaseSerializer getSerializer(int outputStreamSize, TProtocolFactory protocolFactory) {
		return getSerializer(DEFAULT_SAFE_GURANTEED, outputStreamSize, protocolFactory, DEFAULT_TBASE_LOCATOR);
	}

	public static HeaderTBaseSerializer getSerializer(int outputStreamSize, TBaseLocator locator) {
		return getSerializer(DEFAULT_SAFE_GURANTEED, outputStreamSize, new TCompactProtocol.Factory(), locator);
	}

	public static HeaderTBaseSerializer getSerializer(int outputStreamSize, TProtocolFactory protocolFactory, TBaseLocator locator) {
		return getSerializer(DEFAULT_SAFE_GURANTEED, outputStreamSize, protocolFactory, locator);
	}

	// safeGuranteed false을 경우 내부의 ByteArrayOutputStream이 동시성 보장 및 데이터 복사를 전달해서 주지 않음 신중히 사용할 것
	public static HeaderTBaseSerializer getSerializer(boolean safetyGuranteed, int maxSerializeDataSize) {
		return getSerializer(safetyGuranteed, maxSerializeDataSize, new TCompactProtocol.Factory(), DEFAULT_TBASE_LOCATOR);
	}

	public static HeaderTBaseSerializer getSerializer(boolean safetyGuranteed, int maxSerializeDataSize, TProtocolFactory protocolFactory) {
		return getSerializer(safetyGuranteed, maxSerializeDataSize, protocolFactory, DEFAULT_TBASE_LOCATOR);
	}

	public static HeaderTBaseSerializer getSerializer(boolean safetyGuranteed, int maxSerializeDataSize, TBaseLocator locator) {
		return getSerializer(safetyGuranteed, maxSerializeDataSize, new TCompactProtocol.Factory(), locator);
	}

	public static HeaderTBaseSerializer getSerializer(boolean safetyGuranteed, int maxSerializeDataSize, TProtocolFactory protocolFactory, TBaseLocator locator) {
		if (maxSerializeDataSize <= 0) {
			throw new IllegalArgumentException("maxSerializeDataSize must greater than 0.");
		}

		if (protocolFactory == null) {
			throw new NullPointerException("protocolFactory may not be null.");
		}

		if (locator == null) {
			throw new NullPointerException("locator may not be null.");
		}

		ByteArrayOutputStream baos = null;
		if (safetyGuranteed) {
			baos = new ByteArrayOutputStream(maxSerializeDataSize);
		} else {
			baos = new UnsafeByteArrayOutputStream(maxSerializeDataSize);
		}
		
		return new HeaderTBaseSerializer(baos, protocolFactory, locator);
	}
	
	
	// Deserializer 생성
	public static HeaderTBaseDeserializer getDeserializer() {
		return getDeserializer(new TCompactProtocol.Factory(), DEFAULT_TBASE_LOCATOR);
	}
	
	public static HeaderTBaseDeserializer getDeserializer(TProtocolFactory protocolFactory) {
		return getDeserializer(protocolFactory, DEFAULT_TBASE_LOCATOR);
	}

	public static HeaderTBaseDeserializer getDeserializer(TBaseLocator locator) {
		return getDeserializer(new TCompactProtocol.Factory(), locator);
	}

	public static HeaderTBaseDeserializer getDeserializer(TProtocolFactory protocolFactory, TBaseLocator locator) {
		if (protocolFactory == null) {
			throw new NullPointerException("protocolFactory may not be null.");
		}

		if (locator == null) {
			throw new NullPointerException("locator may not be null.");
		}
		
		return new HeaderTBaseDeserializer(protocolFactory, locator);
	}

}
