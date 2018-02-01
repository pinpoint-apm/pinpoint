/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;
import static com.navercorp.pinpoint.common.server.bo.stat.BusinessLogType.TYPE_CODE_BYTE_LENGTH;

import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogType;
import com.navercorp.pinpoint.common.util.BytesUtils;

/**
 * [XINGUANG]
 */
@Component
public class BusinessLogRowKeyDecoder implements RowKeyDecoder<BusinessLogRowKeyComponent>{

	@Override
	public BusinessLogRowKeyComponent decodeRowKey(byte[] rowkey) {
		final String agentId = BytesUtils.safeTrim(BytesUtils.toString(rowkey, 0, AGENT_NAME_MAX_LEN));
		final BusinessLogType businessLogType = BusinessLogType.fromTypeCode(rowkey[AGENT_NAME_MAX_LEN]);
		final String transactionIdANDSpanId = BytesUtils.toString(rowkey, AGENT_NAME_MAX_LEN + TYPE_CODE_BYTE_LENGTH, PinpointConstants.TRANSACTIONID_AND_SPANID_LEN);
		return new BusinessLogRowKeyComponent(agentId, businessLogType, transactionIdANDSpanId);
	}

}
