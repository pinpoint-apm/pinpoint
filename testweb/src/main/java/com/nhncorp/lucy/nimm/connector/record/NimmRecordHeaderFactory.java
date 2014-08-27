package com.nhncorp.lucy.nimm.connector.record;

import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.address.NimmIndividual;
import com.nhncorp.lucy.nimm.connector.record.NimmControlRecord.ControlCode;

/**
 * NimmMockupServer에서 사용하는 NimmRecordHeader를 생성
 * 
 * @author nhn
 *
 */
public final class NimmRecordHeaderFactory {

	/**
	 * Blank private constructor
	 */
	private NimmRecordHeaderFactory() {
		// Blank private constructor
	}
	
	/**
	 * create NimmRecordHeader
	 * (NIMM Control Message, BYE, Duplicated Address)
	 * 
	 * @param remoteAddress remote NIMM address
	 * @param localAddress local NIMM address
	 * @return BYE control message header with duplicated address
	 */
	public static NimmRecordHeader createBYEwithDuplicatedAddr(NimmAddress remoteAddress, NimmAddress localAddress) {
		NimmIndividual[] blankPathThrogh = new NimmIndividual[0];
		int messageId = NimmRecordUtilities.getNextId();

		NimmRecordHeader header = new NimmRecordHeader.Normal(Constants.HEADER.VERSION,
				ControlCode.BYE.getCodeNumber(), 12,
				Constants.HEADER.OPTION.FLAG_NONE,
				Constants.HEADER.CONTROLPARAMETER.NACK_DUPLICATED_ADDRESS_LIST, 0, 0, messageId, 0,
				remoteAddress, localAddress, blankPathThrogh);

		return header;
	}
}
