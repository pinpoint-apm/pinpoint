package com.nhncorp.lucy.nimm.connector.record;

import com.nhncorp.lucy.nimm.connector.record.NimmControlRecord;
import com.nhncorp.lucy.nimm.connector.record.NimmRecordHeader;

import external.org.apache.mina.common.IoBuffer;


/**
 * NimmMockupServer에서 사용하는 NimmControlRecordFactory를 생성
 * 
 * @author nhn
 *
 */
public final class NimmControlRecordFactory {

	/**
	 * Blank private constructor
	 */
	private NimmControlRecordFactory() {
		// Blank private constructor
	}
	
	/**
	 * create NimmControlRecord by given header and payload
	 * 
	 * @param header header of NIMM control message
	 * @param payload payload of NIMM control message
	 * @return NIMM control message
	 */
	public static NimmControlRecord create(NimmRecordHeader header, byte[] payload) {
		return NimmControlRecord.recordCreationFactory(header, IoBuffer.wrap(payload));
	}
}
